package project;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.io.BufferedWriter;

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
        long startTime = System.currentTimeMillis();

        String csvFile = args[0];
        int numClusters;
        String outputFile;
        List<Point> points = readCSV(csvFile);

        if (args.length == 3) {
            numClusters = Integer.parseInt(args[1]);
            outputFile = args[2];
        } else if (args.length == 2) {
            numClusters = estimateOptimalK(points);
            System.out.println("Estimated k: " + numClusters);
            outputFile = args[1];
        } else {
            System.err.println("Usage: java A2_G2_t1 <input csv file> [<num clusters> <output csv file> | <output csv file>]");
            return;
        }

        List<List<Point>> clusters = kMeans(points, numClusters);

        for (int i = 0; i < clusters.size(); i++) {
            System.out.print("Cluster #" + (i + 1) + " => ");
            for (Point p : clusters.get(i)) {
                System.out.print(p.name + " ");
            }
            System.out.println();
        }

        writeClustersToCSV(clusters, outputFile, System.currentTimeMillis() - startTime);
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

    private static List<List<Point>> kMeans(List<Point> points, int numClusters) {
        List<Point> centroids = new ArrayList<>();
        Random rand = new Random();

        Point temp = points.get(rand.nextInt(points.size()));
        centroids.add(temp);

        for (int i = 0; i < numClusters - 1; i++) {
            Point farthestPoint = null;
            double maxDist = -1;

            for (Point currentPoint : points) {
                if (!currentPoint.centroid) {
                    double dist = distance(temp, currentPoint);
                    if (dist > maxDist) {
                        maxDist = dist;
                        farthestPoint = currentPoint;
                    }
                }
            }

            if (farthestPoint != null) {
                farthestPoint.centroid = true;
                centroids.add(farthestPoint);
                temp = farthestPoint;
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
                double sumX = 0, sumY = 0;
                for (Point p : cluster) {
                    sumX += p.x;
                    sumY += p.y;
                }
                Point newCentroid = new Point("", sumX / cluster.size(), sumY / cluster.size(), true);
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

    private static double distance(Point p1, Point p2) {
        return Math.sqrt((p1.x - p2.x) * (p1.x - p2.x) + (p1.y - p2.y) * (p1.y - p2.y));
    }

    private static void writeClustersToCSV(List<List<Point>> clusters, String outputFile, long elapsedTime) {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(outputFile))) {
            // Write the execution time at the top of the file
            writer.write("Execution Time: " + elapsedTime + " milliseconds");
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

    private static int estimateOptimalK(List<Point> points) {
        int kMax = 20; // Set maximum number of clusters to 20
        int B = 10; // Number of reference datasets
        double[] gaps = new double[kMax - 1];

        for (int k = 1; k < kMax; k++) {
            double Wk = calculateWk(points, k);
            double[] Wkbs = new double[B];
            for (int i = 0; i < B; i++) {
                List<Point> randomPoints = generateRandomPoints(points);
                Wkbs[i] = calculateWk(randomPoints, k);
            }
            double Wkb = mean(Wkbs);
            double sk = stdDev(Wkbs) * Math.sqrt(1 + 1.0 / B);
            gaps[k - 1] = Wkb - Wk;
        }

        int optimalK = 1;
        for (int k = 1; k < gaps.length; k++) {
            if (gaps[k] > gaps[k - 1]) {
                optimalK = k + 1;
            }
        }
        while (optimalK > 1) {
            List<List<Point>> clusters = kMeans(points, optimalK);
            boolean hasEmptyCluster = clusters.stream().anyMatch(List::isEmpty);
            if (hasEmptyCluster) {
                optimalK--;
            } else {
                break;
            }
        }
        return optimalK;
    }

    private static double calculateWk(List<Point> points, int k) {
        List<List<Point>> clusters = kMeans(points, k);
        double sum = 0;
        for (List<Point> cluster : clusters) {
            for (Point point : cluster) {
                sum += distance(point, clusterCentroid(cluster));
            }
        }
        return sum;
    }

    private static Point clusterCentroid(List<Point> cluster) {
        double sumX = 0, sumY = 0;
        for (Point p : cluster) {
            sumX += p.x;
            sumY += p.y;
        }
        return new Point("", sumX / cluster.size(), sumY / cluster.size(), true);
    }

    private static List<Point> generateRandomPoints(List<Point> points) {
        List<Point> randomPoints = new ArrayList<>();
        Random rand = new Random();
        double minX = Double.MAX_VALUE, minY = Double.MAX_VALUE;
        double maxX = Double.MIN_VALUE, maxY = Double.MIN_VALUE;

        for (Point p : points) {
            if (p.x < minX) minX = p.x;
            if (p.y < minY) minY = p.y;
            if (p.x > maxX) maxX = p.x;
            if (p.y > maxY) maxY = p.y;
        }

        for (int i = 0; i < points.size(); i++) {
            double x = minX + (maxX - minX) * rand.nextDouble();
            double y = minY + (maxY - minY) * rand.nextDouble();
            randomPoints.add(new Point("", x, y, false));
        }

        return randomPoints;
    }

    private static double mean(double[] values) {
        double sum = 0;
        for (double v : values) {
            sum += v;
        }
        return sum / values.length;
    }

    private static double stdDev(double[] values) {
        double mean = mean(values);
        double sum = 0;
        for (double v : values) {
            sum += (v - mean) * (v - mean);
        }
        return Math.sqrt(sum / values.length);
    }
}
