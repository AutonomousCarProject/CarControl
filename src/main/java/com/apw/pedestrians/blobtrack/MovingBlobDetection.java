package com.apw.pedestrians.blobtrack;

import com.apw.pedestrians.blobdetect.Blob;
import com.apw.pedestrians.Constant;

import java.util.*;

public class MovingBlobDetection implements IMovingBlobDetection {
    //maximum time before unmatched MovingBlob is deleted
    private int maxTimeOffScreen = Constant.MAX_TIME_OFF_SCREEN;
    private int maxTimeOffScreenUnified = 3;
    //maximum distance in pixels between blobs that can be matched
    private int distanceLimitX = Constant.DISTANCE_LIMIT_X;
    private int distanceLimitY = Constant.DISTANCE_LIMIT_Y;
    private int widthChangeLimit = Constant.MAX_CHANGE_WIDTH;
    private int heightChangeLimit = Constant.MAX_CHANGE_HEIGHT;
    private float kernelBandwidth = 15;
    private float maxDistBetweenPointsInCluster = 80;
    private float xDistWeight = 1f;
    private float yDistWeight = 0.25f;
    private float vXWeight = 1.5f;
    private float vYWeight = 0.25f;
    private float distanceUnifyMatchLimit = 60;
    private float percentWidthChangeUnifyMatchLimit = .5f;
    private float percentHeightChangeUnifyMatchLimit = .5f;
    //list of all moving blobs that have been recently tracked
    private List<MovingBlob> movingBlobs;
    private List<MovingBlob> filteredUnifiedBlobs;

    public MovingBlobDetection() {
        movingBlobs = new LinkedList<>();
        filteredUnifiedBlobs = new LinkedList<>();
    }

    public List<MovingBlob> getMovingBlobs(List<Blob> blobList) {
        updateMovingBlobs(blobList);
        return movingBlobs;
    }

    @Override
    public List<MovingBlob> getFilteredUnifiedBlobs(List<MovingBlob> blobList) {
        updateFilteredUnifiedBlobs(blobList);
        return filteredUnifiedBlobs;

    }

    public List<MovingBlob> getUnifiedBlobs(List<MovingBlob> movingBlobs) {
        float[][] finalPoints = new float[movingBlobs.size()][4];

        int index = 0;
        for (MovingBlob movingBlob : movingBlobs) {
            movingBlob.updatePredictedPosition();
            float[] point = {movingBlob.x, movingBlob.y, movingBlob.velocityX, movingBlob.velocityY};
            float distanceMoved = 1000;
            while (distanceMoved > 3) {
                float[] pointTemp = {point[0], point[1], point[2], point[3]};
                point = shift(point, movingBlob, movingBlobs);
                distanceMoved = distBetweenPoints(point, pointTemp);
            }
            finalPoints[index] = point;
            index++;
        }

        // first dimension is what it is because that's how many distances there are
        // second dimension is length 3 because we are storing [distance, index of first point, index of second point]
        float[][] distances = new float[finalPoints.length * (finalPoints.length - 1) / 2][3];
        int j = 0;
        for (int i1 = 0; i1 < finalPoints.length; i1++) {
            for (int i2 = i1 + 1; i2 < finalPoints.length; i2++) {
                float distance = distBetweenPoints(finalPoints[i1], finalPoints[i2]);
                distances[j][0] = distance;
                distances[j][1] = i1;
                distances[j][2] = i2;
                j++;
            }
        }

        Arrays.sort(distances, (o1, o2) -> (int) Math.signum(o1[0] - o2[0]));

        HashMap<Integer, HashSet<Integer>> map = new HashMap<>();
        for (int i = 0; i < distances.length; i++) {
            int point1 = (int) distances[i][1];
            int point2 = (int) distances[i][2];
            HashSet<Integer> pointSet1 = map.get(point1);
            HashSet<Integer> pointSet2 = map.get(point2);
            //System.out.println(pointSet1 + " :" +pointSet2);
            if (pointSet1 != pointSet2 || pointSet1 == null) {
                if (pointSet1 == null && pointSet2 == null) {
                    if (distBetweenPoints(finalPoints[point1], finalPoints[point2]) <= maxDistBetweenPointsInCluster) {
                        HashSet<Integer> newSet = new HashSet<>();
                        newSet.add(point1);
                        newSet.add(point2);
                        map.put(point1, newSet);
                        map.put(point2, newSet);
                    }
                } else if (pointSet1 == null) {
                    boolean canCombine = true;
                    for (int point : pointSet2) {
                        if (distBetweenPoints(finalPoints[point1], finalPoints[point]) > maxDistBetweenPointsInCluster) {
                            canCombine = false;
                        }
                    }
                    if (canCombine) {
                        pointSet2.add(point1);
                        map.remove(point1);
                        map.put(point1, pointSet2);
                    }
                } else if (pointSet2 == null) {
                    boolean canCombine = true;
                    for (int point : pointSet1) {
                        if (distBetweenPoints(finalPoints[point2], finalPoints[point]) > maxDistBetweenPointsInCluster) {
                            canCombine = false;
                        }
                    }
                    if (canCombine) {
                        pointSet1.add(point2);
                        map.remove(point2);
                        map.put(point2, pointSet1);
                    }
                } else {
                    boolean canCombine = true;
                    for (int points1 : pointSet1) {
                        for (int points2 : pointSet2) {
                            if (distBetweenPoints(finalPoints[points1], finalPoints[points2]) > maxDistBetweenPointsInCluster) {
                                canCombine = false;
                            }
                        }
                    }
                    if (canCombine) {
                        pointSet1.addAll(pointSet2);
                        for (int point : pointSet2) {
                            map.remove(point);
                            map.put(point, pointSet1);
                        }
                    }
                }
            }
        }
        LinkedList<MovingBlob> unifiedBlobs = new LinkedList<>();
        for (HashSet<Integer> set : map.values()) {
            HashSet<MovingBlob> blobSet = new HashSet<>();
            for (int i : set) {
                blobSet.add(movingBlobs.get(i));
            }
            unifiedBlobs.add(new UnifiedBlob(blobSet));
        }
        int i = 0;
        for (MovingBlob movingBlob : movingBlobs) {
            if (map.get(i) == null) {
                HashSet<MovingBlob> tempSet = new HashSet<>();
                tempSet.add(movingBlob);
                unifiedBlobs.add(new UnifiedBlob(tempSet));
            }
            i++;
        }
        return unifiedBlobs;
    }

    private float distBetweenBlobs(float[] point, MovingBlob blob1, MovingBlob blob2) {
        float distanceX;
        float distanceY;
        if (point[0] > blob2.x) {
            distanceX = point[0] - (blob2.x + blob2.width);
        } else {
            distanceX = blob2.x - (point[0] + blob1.width);
        }
        if (point[1] > blob2.y) {
            distanceY = point[1] - (blob2.y + blob2.height);
        } else {
            distanceY = blob2.y - (point[1] + blob1.height);
        }
        distanceX = xDistWeight * (float) (Math.pow(Math.max(0, distanceX), 2));
        distanceY = yDistWeight * Math.max(0, distanceY);
        //System.out.println("distanceX: " + distanceX + "   distanceY: " + distanceY);
        float distanceVX = vXWeight * (float) Math.pow(Math.abs(point[2] - blob2.velocityX), 3);
        float distanceVY = vYWeight * Math.abs(point[3] - blob2.velocityY);
        return (float) Math.sqrt(distanceX * distanceX + distanceY * distanceY + distanceVX * distanceVX + distanceVY * distanceVY);
    }

    private float distBetweenPoints(float[] point1, float[] point2) {
        return (float) Math.sqrt(Math.pow(xDistWeight * (point1[0] - point2[0]), 2) + Math.pow(yDistWeight * (point1[1] - point2[1]), 2) +
                Math.pow(vXWeight * (point1[2] - point2[2]), 2) + Math.pow(vYWeight * (point1[3] - point2[3]), 2));
    }

    private float[] shift(float[] point, MovingBlob movingBlob, List<MovingBlob> movingBlobs) {
        float[] shift = {0, 0, 0, 0};
        float weightTotal = 0;
        for (MovingBlob blob : movingBlobs) {
            float distance = distBetweenBlobs(point, movingBlob, blob);
            //float weight1 = (blob.width*blob.height)/(float)Math.pow(this.kernelBandwidth, 2);
            float weight = kernel(distance, this.kernelBandwidth);

            weightTotal += weight;
            shift[0] += (blob.x + blob.width / 2) * weight;
            shift[1] += (blob.y + blob.height / 2) * weight;
            shift[2] += blob.velocityX * weight;
            shift[3] += blob.velocityY * weight;
        }
        for (int i = 0; i < 4; i++) {
            if (weightTotal != 0)
                shift[i] /= weightTotal;
        }
        return shift;
    }

    private float kernel(float distance, float kernelBandwidth) {
        return (float) Math.exp((-Math.pow(distance, 2)) / Math.pow(kernelBandwidth, 2));
    }

    public void updateFilteredUnifiedBlobs(List<MovingBlob> blobList) {
        MovingBlob[][] pairs = new MovingBlob[blobList.size() * filteredUnifiedBlobs.size()][2];
        int j = 0;
        for (int i1 = 0; i1 < filteredUnifiedBlobs.size(); i1++) {
            filteredUnifiedBlobs.get(i1).updatePredictedPosition();
            for (int i2 = 0; i2 < blobList.size(); i2++) {
                pairs[j][0] = filteredUnifiedBlobs.get(i1);
                pairs[j][1] = blobList.get(i2);
                j++;
            }
        }
        Arrays.sort(pairs, (o1, o2) -> {
            float distanceX1 = Math.abs(o1[0].predictedX - (o1[1].x + o1[1].width / 2));
            float distanceY1 = Math.abs(o1[0].predictedY - (o1[1].y + o1[1].height / 2));
            float distance1 = (float) Math.sqrt(distanceX1 * distanceX1 + distanceY1 * distanceY1);
            float distanceX2 = Math.abs(o2[0].predictedX - (o2[1].x + o2[1].width / 2));
            float distanceY2 = Math.abs(o2[0].predictedY - (o2[1].y + o2[1].height / 2));
            float distance2 = (float) Math.sqrt(distanceX2 * distanceX2 + distanceY2 * distanceY2);
            return (int) Math.signum(distance1 - distance2);
        });
        HashSet<MovingBlob> oldBlobs = new HashSet<>(filteredUnifiedBlobs);
        HashSet<MovingBlob> newBlobs = new HashSet<>(blobList);

        for (int i = 0; i < pairs.length; i++) {
            float distanceX = Math.abs(pairs[i][0].predictedX - (pairs[i][1].x + pairs[i][1].width / 2));
            float distanceY = Math.abs(pairs[i][0].predictedY - (pairs[i][1].y + pairs[i][1].height / 2));
            float distance = (float) Math.sqrt(distanceX * distanceX + distanceY * distanceY);
            float percentWChange = Math.abs(pairs[i][0].width - pairs[i][1].width) / Math.max(pairs[i][0].width, pairs[i][1].width);
            float percentHChange = Math.abs(pairs[i][0].width - pairs[i][1].width) / Math.max(pairs[i][0].width, pairs[i][1].width);
            if (distance < distanceUnifyMatchLimit && percentWChange < percentWidthChangeUnifyMatchLimit
                    && percentHChange < percentHeightChangeUnifyMatchLimit
                    && oldBlobs.contains(pairs[i][0]) && newBlobs.contains(pairs[i][1])) {
                matchUnifiedBlob(pairs[i][0], pairs[i][1]);
                oldBlobs.remove(pairs[i][0]);
                newBlobs.remove(pairs[i][1]);
            }
        }

        for (MovingBlob blob : oldBlobs) {
            updateUnmatchedUnifiedBlob(blob);
        }

        filteredUnifiedBlobs.addAll(newBlobs);

    }

    private void matchUnifiedBlob(MovingBlob unifiedBlob, MovingBlob newBlob) {
        //update information based on new position
        unifiedBlob.x = newBlob.x;
        unifiedBlob.y = newBlob.y;
        unifiedBlob.width = newBlob.width;
        unifiedBlob.height = newBlob.height;
        unifiedBlob.age++;
        unifiedBlob.ageOffScreen = 0;
        unifiedBlob.updatePredictedPosition();
    }

    private void updateUnmatchedUnifiedBlob(MovingBlob movingBlob) {
        if (movingBlob.ageOffScreen >= maxTimeOffScreenUnified) {
            //removes blob if it has been gone too long
            this.filteredUnifiedBlobs.remove(movingBlob);
        } else {
            //update position based on most recent velocity
            movingBlob.x += movingBlob.velocityX / 67 * Constant.TIME_DIFFERENCE;
            movingBlob.y += movingBlob.velocityY / 67 * Constant.TIME_DIFFERENCE;

            movingBlob.age++;
            movingBlob.ageOffScreen++;
            movingBlob.updatePredictedPosition();
        }
    }

    private void updateMovingBlobs(List<Blob> blobList) {
        //set of unmatched movingblobs (all are unmatched at start of frame)
        HashSet<MovingBlob> movingBlobSet = new HashSet<>(getMovingBlobs());
        //set of unmatched blobs
        HashSet<Blob> blobSet = new HashSet<>(blobList);
        //queue with shortest distance pairs of movingblobs and blobs in front
        PriorityQueue<BlobPair> queue = new PriorityQueue<>();
        for (Blob blob : blobList) {
            for (MovingBlob movingBlob : this.movingBlobs) {
                //creates pairs in queue of blobs & moving blobs with same color within 100 pixels
                movingBlob.updatePredictedPosition();
                if (blob.color.getColor() == movingBlob.color.getColor()) {
                    float distanceX = Math.abs(movingBlob.predictedX - (blob.x + blob.width / 2));
                    float distanceY = Math.abs(movingBlob.predictedY - (blob.y + blob.height / 2));
                    float distance = (float) Math.sqrt(distanceX * distanceX + distanceY * distanceY);
                    float widthChange = Math.abs(movingBlob.width - blob.width);
                    float heightChange = Math.abs(movingBlob.height - blob.height);
                    if (distanceX <= distanceLimitX && distanceY <= distanceLimitY &&
                            widthChange <= widthChangeLimit && heightChange <= heightChangeLimit) {
                        queue.add(new BlobPair(distance, blob, movingBlob));
                    }
                }
            }
        }
        //matches closest pairs until it runs out of movingBlobs, blobs, or pairs
        while (!movingBlobSet.isEmpty() && !blobSet.isEmpty() && !queue.isEmpty()) {
            //finds shortest pair in queue
            BlobPair pair = queue.peek();
            //checks if neither blobs are matched already
            if (movingBlobSet.contains(pair.oldBlob) && blobSet.contains(pair.newBlob)) {
                //matches blobs and updates sets and queue
                matchBlob(pair.oldBlob, pair.newBlob);
                movingBlobSet.remove(pair.oldBlob);
                blobSet.remove(pair.newBlob);
                queue.remove();
            } else {
                //if either blob is matched, removes pair from queue
                queue.remove();
            }
        }
        //updates unmatched MovingBlobs
        for (MovingBlob movingBlob : movingBlobSet) {
            updateUnmatched(movingBlob);
        }
        
        //creates new MovingBlobs for unmatched blobs
        for (Blob blob : blobSet) {
            this.movingBlobs.add(new MovingBlob(blob));
        }
    }

    private void matchBlob(MovingBlob movingBlob, Blob newBlob) {
        //update information based on new position
        calculateVelocity(movingBlob, newBlob);
        movingBlob.x = newBlob.x;
        movingBlob.y = newBlob.y;
        movingBlob.width = newBlob.width;
        movingBlob.height = newBlob.height;
        movingBlob.age++;
        movingBlob.ageOffScreen = 0;
        movingBlob.updatePredictedPosition();
    }

    private void updateUnmatched(MovingBlob movingBlob) {
        if (movingBlob.ageOffScreen >= maxTimeOffScreen) {
            //removes blob if it has been gone too long
            getMovingBlobs().remove(movingBlob);
        } else {
            //update position based on most recent velocity
            movingBlob.x += movingBlob.velocityX / 67 * Constant.TIME_DIFFERENCE;
            movingBlob.y += movingBlob.velocityY / 67 * Constant.TIME_DIFFERENCE;

            movingBlob.age++;
            movingBlob.ageOffScreen++;
            movingBlob.updatePredictedPosition();
        }
    }

    private void calculateVelocity(MovingBlob movingBlob, Blob newBlob) {
        float centerXOld = movingBlob.x + movingBlob.width / 2;
        float centerYOld = movingBlob.y + movingBlob.height / 2;
        float centerXNew = newBlob.x + newBlob.width / 2;
        float centerYNew = newBlob.y + newBlob.width / 2;
        float movementX = centerXNew - centerXOld;
        float movementY = centerYNew - centerYOld;

        //standardize to 15 fps
        movementX *= 67;
        movementX /= Constant.TIME_DIFFERENCE;
        movementY *= 67;
        movementY /= Constant.TIME_DIFFERENCE;

        float tempVelX = movingBlob.velocityX;
        float tempVelY = movingBlob.velocityY;
        //finds average of previous velocity and velocity between last and current frame

        movingBlob.velocityX += movementX;
        movingBlob.velocityX /= 2;
        movingBlob.velocityChangeX = Math.abs(tempVelX - movingBlob.velocityX);

        movingBlob.velocityY += movementY;
        movingBlob.velocityY /= 2;
        movingBlob.velocityChangeY = Math.abs(tempVelY - movingBlob.velocityY);
    }

    public List<MovingBlob> getMovingBlobs() {
        return movingBlobs;
    }
}
