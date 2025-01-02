package project;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.io.BufferedWriter;
import java.time.Duration;
import java.time.Instant;

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

        List<List<Point>> clusters = kMeans(points, numClusters);

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
        double[] silhouetteScores = new double[maxK];

        for (int k = 2; k <= maxK; k++) {
            List<List<Point>> clusters = kMeans(points, k);
            silhouetteScores[k - 2] = calculateSilhouetteScore(clusters);
        }

        int optimalK = 2;
        double maxScore = silhouetteScores[0];
        for (int i = 1; i < maxK - 1; i++) {
            if (silhouetteScores[i] > maxScore) {
                maxScore = silhouetteScores[i];
                optimalK = i + 2;
            }
        }

        return optimalK;
    }

    private static double calculateSilhouetteScore(List<List<Point>> clusters) {
        double totalSilhouetteScore = 0.0;
        int totalPoints = 0;

        for (List<Point> cluster : clusters) {
            for (Point point : cluster) {
                double a = calculateAverageDistance(point, cluster);
                double b = Double.MAX_VALUE;
                for (List<Point> otherCluster : clusters) {
                    if (otherCluster != cluster) {
                        double distance = calculateAverageDistance(point, otherCluster);
                        if (distance < b) {
                            b = distance;
                        }
                    }
                }
                totalSilhouetteScore += (b - a) / Math.max(a, b);
            }
            totalPoints += cluster.size();
        }

        return totalSilhouetteScore / totalPoints;
    }

    private static double calculateAverageDistance(Point point, List<Point> cluster) {
        double sumDistance = 0.0;
        for (Point otherPoint : cluster) {
            if (point != otherPoint) {
                sumDistance += distance(point, otherPoint);
            }
        }
        return sumDistance / (cluster.size() - 1);
    }

    private static List<List<Point>> kMeans(List<Point> points, int numClusters) {
        List<Point> centroids = new ArrayList<>();
        Random rand = new Random();

        // KMeans++ initialization
        Point firstCentroid = points.get(rand.nextInt(points.size()));
        centroids.add(firstCentroid);
        
        for (int i = 1; i < numClusters; i++) {
            double[] distances = new double[points.size()];
            double totalDistance = 0.0;

            for (int j = 0; j < points.size(); j++) {
                Point point = points.get(j);
                double minDist = Double.MAX_VALUE;
                for (Point centroid : centroids) {
                    double dist = distance(point, centroid);
                    if (dist < minDist) {
                        minDist = dist;
                    }
                }
                distances[j] = minDist * minDist; // Square of distance
                totalDistance += distances[j];
            }

            double randomValue = rand.nextDouble() * totalDistance;
            double cumulativeDistance = 0.0;

            for (int j = 0; j < points.size(); j++) {
                cumulativeDistance += distances[j];
                if (cumulativeDistance >= randomValue) {
                    centroids.add(points.get(j));
                    break;
                }
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
