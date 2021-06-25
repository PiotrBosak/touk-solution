create table movies
(
    id    integer primary key,
    title varchar(50) not null
);

create table rooms
(
    id            integer primary key,
    rows          integer not null,
    seats_per_row integer not null
);

create table screenings
(
    id       integer primary key,
    movie_id integer not null,
    room_id  integer not null,
    screening_date date,
    constraint movie_fkey foreign key (movie_id)
        references movies (id),
    constraint room_fkey foreign key (room_id)
        references rooms (id)

);

create table clients
(
    id      integer primary key,
    name    varchar(50) not null,
    surname varchar(50) not null
);


create table reservations
(
    id           integer primary key,
    client_id      integer not null,
    screening_id integer not null,
    total_cost float not null ,
    constraint client_fkey foreign key (client_id)
        references clients (id),
    constraint screening_fkey foreign key (screening_id)
        references screenings (id)
);


create table taken_seats
(
    id             integer primary key,
    reservation_id integer not null,
    row integer not null ,
    seat_in_row integer not null ,
    ticket_type    varchar(50),
    constraint screening_fkey foreign key (reservation_id)
        references reservations (id)
);

