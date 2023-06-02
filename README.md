# flight-booking
A flight booking app made in akka and Play framework using reactive mongo as database for storing booking and flight data(which will be ingested via a csv file using akka streams)

Use command 'sbt runProd' to launch the Play application so that akka stream module runs eagerly on app startup.

Dataset for the application is downloaded from here : https://figshare.com/articles/dataset/flights_csv/9820139/1
