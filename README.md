## Simple POC to demonstrate problem with image resizing, this app does 3 things

1. Convert Base64 Image to InputStream
2. Resize Image -- problem occurs in native mode
3. Save image to disk

## To reproduce image resize bug
`./mvnw verify -Pnative`



