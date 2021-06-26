create database touksolution;
\c touksolution;
create table movies
(
    id    serial primary key,
    title varchar(50) not null
);

create table rooms
(
    id            serial primary key,
    rows          integer not null,
    seats_per_row integer not null
);

create table screenings
(
    id       serial primary key,
    movie_id integer not null,
    room_id  integer not null,
    screening_time timestamp ,
    constraint movie_fkey foreign key (movie_id)
        references movies (id),
    constraint room_fkey foreign key (room_id)
        references rooms (id)

);


create table reservations
(
    id           serial primary key,
    client_name varchar (50) not null,
    client_surname varchar (50) not null,
    screening_id integer not null,
    total_cost float not null ,
    constraint screening_fkey foreign key (screening_id)
        references screenings (id)
);


create table taken_seats
(
    id             serial primary key,
    reservation_id integer not null,
    row integer not null ,
    seat_in_row integer not null ,
    ticket_type    varchar(50),
    constraint screening_fkey foreign key (reservation_id)
        references reservations (id)
);
insert into movies(id, title) values (1,'Godfather');
insert into movies(id, title) values (2,'Godfather 2');

insert into rooms(id, rows, seats_per_row)  values (1,5,5);
insert into rooms(id, rows, seats_per_row)  values (2,10,10);

insert into screenings(id, movie_id, room_id, screening_time)
VALUES(1,1,1,current_timestamp + (2 * interval '1 hour'));
insert into screenings(id, movie_id, room_id, screening_time)
VALUES(2,2,2,current_timestamp + (10 * interval '1 minute'));


insert into reservations
(client_name, client_surname, screening_id, total_cost) values('Jan', 'Dzięrzecki', 1,50.0);
insert into taken_seats(reservation_id, row, seat_in_row, ticket_type)
values (1,3,3,'child');
insert into taken_seats(reservation_id, row, seat_in_row, ticket_type)
values (1,4,5,'adult');
