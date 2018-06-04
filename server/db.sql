create database whisper;

use whisper;

create table users(
    userid int(32) not null primary key auto_increment,
    email varchar(64) not null unique key,
    passwd varchar(64) not null,
    publickey text not null,
    privatekey text not null
);

create table friends(
    id1 int(32) ,
    id2 int(32),
    valid boolean not null,
    foreign key (id1) references users(userid),
    foreign key (id2) references users(userid)
);