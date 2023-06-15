# flight-booking
A flight booking app made in akka and Play framework using reactive mongo as database for storing booking and flight data(which will be ingested via a csv file using akka streams)

Use command 'sbt runProd' to launch the Play application so that akka stream module runs eagerly on app startup.

Dataset for the application is downloaded from here : https://figshare.com/articles/dataset/flights_csv/9820139/1

# Application architecture diagram


![ticket_booking_pipeline](https://github.com/sarthak2897/flight-booking/assets/60536515/b3d6d8ed-5ddb-49c4-864d-2c5af1195ea5)
