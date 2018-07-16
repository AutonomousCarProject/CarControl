Group 1

Purpose: 
Get RGB data from camera and convert to YUV (In actuality, only hue is outputed).

How to Use:
1. Make instance of class Image of interface IImage.
2. Call readCam to fill image array.
3. Call getImage to get image array.
4. Use image array for whatever your heart desires.

Note:
    The method Image.getImage()[][].getColor() returns an integer value from 0 - 5 where
        0: red
        1: green
        2: blue
        3: grey
        4: black
        5: white