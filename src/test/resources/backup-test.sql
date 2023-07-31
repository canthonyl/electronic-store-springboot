

create sequence cart_id_seq as bigint;
create table cart (
     id bigint primary key
);

create sequence cart_item_id_seq as bigint;
create table cart_item (
     id bigint primary key,
     cart_id bigint,
     quantity int,
     foreign key(cart_id) references cart(id)
);