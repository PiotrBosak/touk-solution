echo "listing screenings"
curl --data ' {
    "start" : "1999-01-08 04:05:06",
    "finish" : "2023-01-08 04:05:06"
}' --header 'Content-Type: application/json' --request GET http://localhost:8080/screenings
for i in {1..5}
do
  printf "\n"
done
echo "picking screening"
curl http://localhost:8080/screenings/pick/1
for i in {1..5}
do
  printf "\n"
done
echo "making reservation, successful"
curl --verbose --data ' {
    "screeningId" : 1,
    "pickedSeats" : [
        {
        "row" : 2,
        "seatInRow" : 4,
        "ticketType" : "adult"
        },
        {
            "row" : 2,
            "seatInRow" : 5,
            "ticketType" : "child"
        }
    ],
    "name" : "Jan",
    "surname" : "Kowąlski"
}' --header 'Content-Type: application/json' --request POST http://localhost:8080/reservations/make
for i in {1..5}
do
  printf "\n"
done
echo 'making reservation, unsuccessful, wrong ticket type '
curl --verbose --data ' {
    "screeningId" : 1,
    "pickedSeats" : [
        {
        "row" : 2,
        "seatInRow" : 4,
        "ticketType" : "adult"
        },
        {
            "row" : 2,
            "seatInRow" : 5,
            "ticketType" : "chil"
        }
    ],
    "name" : "Jan",
    "surname" : "Kowąlski"
}' --header 'Content-Type: application/json' --request POST http://localhost:8080/reservations/make
for i in {1..5}
do
  printf "\n"
done
echo 'making reservation, unsuccessful,left one seat empty'
curl --verbose --data ' {
    "screeningId" : 1,
    "pickedSeats" : [
        {
        "row" : 2,
        "seatInRow" : 4,
        "ticketType" : "adult"
        },
        {
            "row" : 2,
            "seatInRow" : 5,
            "ticketType" : "chil"
        }
    ],
    "name" : "Jan",
    "surname" : "Kowąlski"
}' --header 'Content-Type: application/json' --request POST http://localhost:8080/reservations/make
