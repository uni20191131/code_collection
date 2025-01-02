import java.io.*;
import java.util.*;
import java.time.*;

class Point {
    String name;
    double x;
    double y;
    boolean centroid;

    Point(String name, double x, double y, boolean centroid) {
        this.name = name;
        this.x = x;
        this.y = y;
        this.centroid = centroid;
    }
}

public class A2_G2_t1 {

    public static void main(String[] args) {
        Instant start = Instant.now();

        String csvFile = args[0];
        int numClusters = -1;
        String outputFile = null;

        if (args.length > 2) {
            numClusters = Integer.parseInt(args[1]);
            outputFile = args[2];
        } else if (args.length > 1) {
            outputFile = args[1];
        }

        List<Point> points = readCSV(csvFile);

        boolean estimatedK = false;
        if (numClusters == -1) {
            numClusters = findOptimalK(points);
            estimatedK = true;
        }

        List<List<Point>> clusters = kMeansPlusPlus(points, numClusters);

        if (estimatedK) {
            System.out.println("estimated k: " + numClusters);
        }

        for (int i = 0; i < clusters.size(); i++) {
            System.out.print("Cluster #" + (i + 1) + " => ");
            for (Point p : clusters.get(i)) {
                System.out.print(p.name + " ");
            }
            System.out.println();
        }
        
        Instant end = Instant.now();
        Duration timeElapsed = Duration.between(start, end);
        if (outputFile != null) {
            writeClustersToCSV(clusters, outputFile, timeElapsed);
        }
    }

    private static List<Point> readCSV(String csvFile) {
        List<Point> points = new ArrayList<>();
        String line;
        try (BufferedReader br = new BufferedReader(new FileReader(csvFile))) {
            while ((line = br.readLine()) != null) {
                String[] values = line.split(",");
                String name = values[0].trim();
                double x = Double.parseDouble(values[1].trim());
                double y = Double.parseDouble(values[2].trim());
                points.add(new Point(name, x, y, false));
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return points;
    }

    private static int findOptimalK(List<Point> points) {
        int maxK = 35;
        double[] sse = new double[maxK];

        for (int k = 1; k <= maxK; k++) {
            List<List<Point>> clusters = kMeansPlusPlus(points, k);
            sse[k - 1] = calculateSSE(clusters);
        }

        // 엘보우 포인트 찾기
        int optimalK = 1;
        double maxSlopeChange = 0;

        for (int i = 1; i < maxK - 1; i++) {
            double slope1 = sse[i - 1] - sse[i];
            double slope2 = sse[i] - sse[i + 1];
            double slopeChange = slope1 - slope2;

            if (slopeChange > maxSlopeChange) {
                maxSlopeChange = slopeChange;
                optimalK = i + 1;
            }
        }

        return optimalK;
    }

    private static double calculateSSE(List<List<Point>> clusters) {
        double sse = 0.0;
        for (List<Point> cluster : clusters) {
            Point centroid = calculateCentroid(cluster);
            for (Point point : cluster) {
                sse += Math.pow(distance(point, centroid), 2);
            }
        }
        return sse;
    }

    private static List<List<Point>> kMeansPlusPlus(List<Point> points, int numClusters) {
        List<Point> centroids = new ArrayList<>();
        Random rand = new Random();

        Point firstCentroid = points.get(rand.nextInt(points.size()));
        centroids.add(firstCentroid);

        for (int i = 1; i < numClusters; i++) {
            double[] distances = new double[points.size()];
            double totalDistance = 0.0;

            for (int j = 0; j < points.size(); j++) {
                Point p = points.get(j);
                double minDistance = Double.MAX_VALUE;
                for (Point c : centroids) {
                    double dist = distance(p, c);
                    if (dist < minDistance) {
                        minDistance = dist;
                    }
                }
                distances[j] = minDistance;
                totalDistance += minDistance;
            }

            double randDist = rand.nextDouble() * totalDistance;
            double cumulativeDistance = 0.0;
            Point nextCentroid = null;
            for (int j = 0; j < points.size(); j++) {
                cumulativeDistance += distances[j];
                if (cumulativeDistance >= randDist) {
                    nextCentroid = points.get(j);
                    break;
                }
            }

            if (nextCentroid != null) {
                nextCentroid.centroid = true;
                centroids.add(nextCentroid);
            }
        }

        List<List<Point>> clusters = new ArrayList<>();
        for (int i = 0; i < numClusters; i++) {
            clusters.add(new ArrayList<>());
        }

        boolean converged = false;
        while (!converged) {
            for (Point point : points) {
                double minDist = Double.MAX_VALUE;
                int clusterIndex = 0;
                for (int i = 0; i < centroids.size(); i++) {
                    double dist = distance(point, centroids.get(i));
                    if (dist < minDist) {
                        minDist = dist;
                        clusterIndex = i;
                    }
                }
                clusters.get(clusterIndex).add(point);
            }

            List<Point> newCentroids = new ArrayList<>();
            for (List<Point> cluster : clusters) {
                Point newCentroid = calculateCentroid(cluster);
                newCentroids.add(newCentroid);
            }

            converged = true;
            for (int i = 0; i < centroids.size(); i++) {
                if (distance(centroids.get(i), newCentroids.get(i)) > 0.001) {
                    converged = false;
                    break;
                }
            }

            centroids = newCentroids;

            if (!converged) {
                for (List<Point> cluster : clusters) {
                    cluster.clear();
                }
            }
        }
        return clusters;
    }

    private static Point calculateCentroid(List<Point> cluster) {
        double sumX = 0, sumY = 0;
        for (Point p : cluster) {
            sumX += p.x;
            sumY += p.y;
        }
        return new Point("", sumX / cluster.size(), sumY / cluster.size(), true);
    }

    private static double distance(Point p1, Point p2) {
        return Math.sqrt((p1.x - p2.x) * (p1.x - p2.x) + (p1.y - p2.y) * (p1.y - p2.y));
    }

    private static void writeClustersToCSV(List<List<Point>> clusters, String outputFile, Duration timeElapsed) {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(outputFile))) {
            writer.write("Execution Time: " + timeElapsed.toMillis() + " ms");
            writer.newLine();
            for (int i = 0; i < clusters.size(); i++) {
                List<Point> cluster = clusters.get(i);
                for (Point point : cluster) {
                    writer.write(point.name + "," + point.x + "," + point.y + "," + (i + 1));
                    writer.newLine();
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
