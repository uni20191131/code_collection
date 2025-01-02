import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Comparator;

class Point {
    String id;
    double x, y, z;
    int cluster;
    double silhouette_score;

    Point(String id, double x, double y) {
        this.id = id;
        this.x = x;
        this.y = y;
        this.cluster = 0; // 0 means unclassified, -1 means noise
        this.silhouette_score = 0.0; //Initialize silhoutte score;
    }

    // calculate Euclidean distance between this point and other point.
    double distance(Point other) {
        return Math.sqrt(Math.pow(this.x - other.x, 2) + Math.pow(this.y - other.y, 2));
    }
}

class DBSCAN {
    private int mu; //minPts
    private double eps;
    public int cluster;

    public DBSCAN(int mu, double eps) {
        this.mu = mu;
        this.eps = eps;
        this.cluster = 1;
    }

    // Run the DB-SCAN algorithm on the given list of points    
    public void run(List<Point> points) {
        for (Point point : points) {
            if (point.cluster == 0) {  // if clusterID of point is 0 => represents unclassfied.
                if (expandCluster(point, points)) {
                    cluster++;
                    //System.out.println("one cluster find : " + cluster); // for debugging
                }
            }
        }
        calculateSilhouetteScore(points);
    }

    // Expancd the cluster starting from the given point
    private boolean expandCluster(Point point, List<Point> points) {
        List<Point> seeds = regionQuery(point, points);  // List of points located at a distance less than eps
        if (seeds.size() < mu) {  // If the size is smaller than minPts, cluster cannot be formed.
            point.cluster = -1; // Mark as noise
            return false;
        }

        point.cluster = cluster;
        for (Point seed : seeds) {  // Make them belong to the same cluster
            seed.cluster = cluster;
            //System.out.println("point : " + seed.id); // for debugging
        }
        //System.out.println("current point : " + point.id);  // for debugging
        seeds.remove(point);

        // Expand the cluster by checking whether other points belonging to the cluster are core points.
        while (!seeds.isEmpty()) {   
            Point current = seeds.get(0);
            //System.out.println("current point : " + current.id); // for debugging
            List<Point> result = regionQuery(current, points);
            if (result.size() >= mu) {  // this means current point is core point
                for (Point p : result) {  // expand cluster with new core point
                    if (p.cluster == 0 || p.cluster == -1) {  // check neighbor of new core point is whether unclassified or noise.
                        if (p.cluster == 0) {  // if neighbor fo new core point is unclassified, add on seed to check can be core point.
                            seeds.add(p);
                        }
                        p.cluster = cluster;
                    }
                }
            }
            seeds.remove(0);
        }
        return true;
    }

    // Find all points within eps distance of the given point
    private List<Point> regionQuery(Point point, List<Point> points) {
        List<Point> result = new ArrayList<>();
        for (Point p : points) {
            if (point.distance(p) <= eps) {
                result.add(p);
            }
        }
        return result;
    }

    // when clustering done, calculate the silhouetteScore of each point.
    private static void calculateSilhouetteScore(List<Point> points) {
        for (Point point : points) {
            if (point.cluster == -1) {  // Noise points do not contribute to the silhouette score
                continue;
            }

            double a = calculateAverageDistance(point, points, point.cluster);
            double b = Double.MAX_VALUE;

            for (int clusterId = 1; clusterId <= getMaxCluster(points); clusterId++) {
                if (clusterId != point.cluster) {
                    double bTemp = calculateAverageDistance(point, points, clusterId);
                    if (bTemp < b) {
                        b = bTemp;
                    }
                }
            }

            point.silhouette_score = (b - a) / Math.max(a, b);
        }
    }

    // calculate aveage distance between current point and another point which in another cluster
    private static double calculateAverageDistance(Point point, List<Point> points, int clusterId) {
        double totalDistance = 0.0;
        int count = 0;

        for (Point other : points) {
            if (other.cluster == clusterId && !other.equals(point)) {
                totalDistance += point.distance(other);
                count++;
            }
        }

        return count == 0 ? 0 : totalDistance / count;
    }

    // get the number of cluster
    private static int getMaxCluster(List<Point> points) {
        int maxCluster = 0;
        for (Point point : points) {
            if (point.cluster > maxCluster) {
                maxCluster = point.cluster;
            }
        }
        return maxCluster;
    }
}

public class A2_G11_t2 {
    public static void main ( String[] args ) {
        if (args.length < 2 || args.length > 3) {
            System.out.println("Usage: java DBSCAN <file_path> <eps> <mu>" 
                                + " Or java DBSCAN <file_path> <eps/mu>");
            return;
        }

        String filePath = args[0];
        double eps = 0;
        int mu = 0;

        List<Point> points = readFile(filePath);  // read file containing data points.
        //System.out.println("read file done"); //for debugging

        if (args.length == 2) {
            try {
                if (args[1].contains(".")) {    // if args[1] contain '.', then args[1] represents epsilon
                    eps = Double.parseDouble(args[1]);
                    mu = optimizationMu(eps, points);  // find optimization mu value
                    System.out.println("Estimated MinPts : " + mu);
                } else {                          // else, args[1] represents mu(minPts)
                    mu = Integer.parseInt(args[1]);
                    eps = optimizationEps(mu, points);  // find optimization eps value
                    System.out.println("Estimated eps : " + eps);
                }
            } catch (NumberFormatException e) {     // Error handling
                System.out.println("Invalid parameter: " + args[1]);
                return;
            }
        }

        if (args.length == 3) {     // this mean args[1] represents mu , args[2] represents eps
            try {
                mu = Integer.parseInt(args[1]);
                eps = Double.parseDouble(args[2]);
            } catch (NumberFormatException e) {    // Error handling
                System.out.println("Invalid parameters: " + args[1] + ", " + args[2]);
                return;
            }
        }


        DBSCAN dbscan = new DBSCAN(mu, eps);
        dbscan.run(points);  // start clustering
        //System.out.println("DBSCAN done");  // for debugging

        int noiseCount = 0;
        int clusterCount = dbscan.cluster-1;

        List<StringBuilder> clusters = new ArrayList<>();
        for (int i = 0; i <= clusterCount ; i++) {
            clusters.add(new StringBuilder("Cluster #" + i + " => "));
        }

        for (Point point : points) {
            if (point.cluster == -1) {
                noiseCount++;
            } else {
                clusters.get(point.cluster).append(point.id).append(" ");
            }
        }

        System.out.println("Number of clusters : " + clusterCount);
        System.out.println("Number of noise : " + noiseCount);

        //double averageS_score = averageSilhouetteScore(points);  // for debugging check silhouttescore
        //System.out.println("Silhouette Score : " + averageS_score);  // for debugging check silhouttescore

        for (int i = 1; i <= clusterCount; i++) {
            System.out.println(clusters.get(i).toString().trim());
        }

        //System.out.println("Done");  // for debugging
    }

    // read the dataset from the input filePath, and return the list of points.
    private static List<Point> readFile(String filePath) {
        List<Point> points = new ArrayList<>();
        try (BufferedReader br = new BufferedReader(new FileReader(filePath))) {
            String line;
            while ((line = br.readLine()) != null) {
                String[] values = line.split(",");
                String id = values[0];
                double x = Double.parseDouble(values[1]);
                double y = Double.parseDouble(values[2]);
                points.add(new Point(id, x, y));
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return points;
    }


    // Find optimization mu by three Method with silhouette_score
    /*
     * The clustering is performed with each 'mu' value, and the average silhouette score is compared.
     * case 1 : round down ln(n)
     * case 2 : round up ln(n)
     * case 3 : (dataset)dimension * 2
     */
    private static int optimizationMu(double eps, List<Point> points){
        int dimension = 2;
        int datasize = points.size();
        int[] muCandidate = {
            (int) Math.floor(Math.log(datasize)), //case1 : round down ln(datasize)
            (int) Math.ceil(Math.log(datasize)), //case2 : round up ln(datasize)
            (dimension * 2) //case3 : dimension * 2
        };

        int optimalmu = muCandidate[0];
        double bestSilhouetteScore = Double.NEGATIVE_INFINITY;

        for (int mu : muCandidate) {
            DBSCAN dbscan = new DBSCAN(mu, eps);
            dbscan.run(points);
            double averageS_score = averageSilhouetteScore(points);
            //System.out.println("mu : " + mu + " eps : " + eps + " S_score = " + averageS_score);  // for debugging
            if (averageS_score > bestSilhouetteScore) {
                bestSilhouetteScore = averageS_score;
                optimalmu = mu;
            }
            for (Point point : points) {   // initialize clusterId of each Point after once clustering Done.
                point.cluster = 0;
            }
        }

        return optimalmu;
    }

    // Calculate aveage SilhouetteScore
    private static double averageSilhouetteScore(List<Point> points) {
        int size = points.size();
        double t_score = 0.0;
        for (Point p : points){
            if (p.cluster != -1){
                t_score += p.silhouette_score;
            //    System.out.println("id : " + p.id + " s_score : " + p.silhouette_score);  // for debugging
            }
        }
        return t_score / size;
    }

    

    // Find optimization eps by k-distance method
    // To do, not complete
    private static double optimizationEps(int mu, List<Point> points){
        List<Double> kDistances = new ArrayList<>();

        // Calculate k-distances for each point
        for (Point point : points) {
            List<Double> distances = new ArrayList<>();
            for (Point other : points) {
                if (!point.equals(other)) {
                    distances.add(point.distance(other));
                }
            }
            Collections.sort(distances);
            kDistances.add(distances.get(mu - 1));
        }

        // Sort k-distances in descending order
        Collections.sort(kDistances, Comparator.reverseOrder());

        //System.out.println("kDistances after sorting: " + kDistances); //for debugging

        return findelbowpoint(kDistances);
    }

    // method that find elbowpoint at k-th distance plot
    /*
     * 1. Draw a Line Between the First and Last Points
     * 2. Calculate the Distance from Each Point to the Line
     * 3. Find the Largest Distance
     * 4. Determine the Elbow Point
     */
    private static double findelbowpoint(List<Double> kdist){
        int datasize = kdist.size();
        Point firstP = new Point("fp",0, kdist.get(0));
        Point lastP = new Point("lp",datasize-1, kdist.get(datasize-1));

        double maxDist = -1;
        int elbowPointIndex = -1;

        for (int i = 0; i < datasize; i++) {
            Point currentP = new Point("np",i, kdist.get(i));
            double distance = calculateDistanceFromLine(firstP, lastP, currentP);
            if (distance > maxDist) {
                maxDist = distance;
                elbowPointIndex = i;
            }
        }

        return kdist.get(elbowPointIndex);
    }

    // method that Calculate the Distance from Each Point to the Line
    private static double calculateDistanceFromLine(Point fp, Point lp, Point cp){
        double normalLength = Math.sqrt(Math.pow(lp.x - fp.x, 2) 
                                + Math.pow(lp.y - fp.y, 2));
        double dist =  Math.abs((cp.x - fp.x) * (lp.y - fp.y) 
                        - (cp.y - fp.y) * (lp.x - fp.x)) / normalLength;
        return dist;
    }
}