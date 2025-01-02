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
        String csvFile = args[0];
        int numClusters = Integer.parseInt(args[1]);
        String outputFile = args[2];
        List<Point> points = readCSV(csvFile);

        List<List<Point>> clusters = kMeans(points, numClusters);

        for (int i = 0; i < clusters.size(); i++) {
            System.out.print("Cluster #" + (i + 1) + " => ");
            for (Point p : clusters.get(i)) {
                System.out.print(p.name + " ");
            }
            System.out.println();
        }
        
         writeClustersToCSV(clusters, outputFile);
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
        centroids.add(temp);//랜덤으로 한점만
        
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
            farthestPoint.centroid = true; // 이 점을 centroid로 표시
            centroids.add(farthestPoint);
            temp = farthestPoint; // 가장 먼 점을 새로운 기준점으로 설정
        }
    }

        
        //가장 먼 거리의 점 구하기

        List<List<Point>> clusters = new ArrayList<>();
        for (int i = 0; i < numClusters; i++) {
            clusters.add(new ArrayList<>());
        }//클러스터 미리 만들기

        boolean converged = false;
        while (!converged) {
            // Assign points to the nearest centroid
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

            //센트로이드 업데이트
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

            // 센트로이드가 수렴하는지 확인
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
        return Math.sqrt(  (p1.x - p2.x)*(p1.x - p2.x) + (p1.y - p2.y) * (p1.y - p2.y) );
    }//거리 계산해 주는 함수
    
        //여기는 아웃풋 파일 만들어주는 구간
        private static void writeClustersToCSV(List<List<Point>> clusters, String outputFile) {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(outputFile))) {
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