drop sequence if exists product_category_id_seq;
drop sequence if exists product_id_seq;
drop sequence if exists shopping_cart_id_seq;
drop sequence if exists shopping_cart_item_id_seq;
drop sequence if exists discount_rule_id_seq;
drop sequence if exists discount_rule_setting_id_seq;

drop table if exists shopping_cart_item;
drop table if exists shopping_cart;
drop table if exists product;
drop table if exists product_category;
drop table if exists discount_rule_setting;
drop table if exists discount_rule;

create sequence product_category_id_seq as bigint;
create sequence product_id_seq as bigint;
create sequence shopping_cart_id_seq as bigint;
create sequence shopping_cart_item_id_seq as bigint;
create sequence discount_rule_id_seq as bigint;
create sequence discount_rule_setting_id_seq as bigint;

create table product_category (
     id bigint primary key,
     name varchar(255) not null
);

create table product (
     id bigint primary key,
     category_id bigint not null,
     name varchar(255) not null,
     description varchar(255),
     price numeric(20, 2),
     foreign key(category_id) references product_category(id)
);

create table shopping_cart (
     id bigint primary key
);

create table shopping_cart_item (
     id bigint default(next value for shopping_cart_item_id_seq) default on null primary key,
     shopping_cart_id bigint not null,
     product_id bigint not null,
     quantity int,
     price numeric(20, 2),
     amount_before_discount numeric(20, 2),
     discount_amount numeric(20, 2),
     amount numeric(20, 2),
     foreign key(shopping_cart_id) references shopping_cart(id),
     foreign key(product_id) references product(id)
);

create table discount_rule(
    id bigint primary key,
    threshold_unit bigint,
    threshold_unit_type enum('Qty','Amount') not null,
    applicable_unit bigint,
    applicable_unit_type enum('Qty','Amount') not null,
    applicable_discount numeric(20, 2),
    override_amount numeric(20, 2),
    description varchar(255),
    rule_group_id bigint
);

create table discount_rule_setting(
    id bigint primary key,
    rule_group_id bigint not null,
    category_id bigint,
    product_id bigint,
    setting json(10000)
);


create sequence shopping_cart_test_item_id_seq as bigint;

create table shopping_cart_test_item(
    id bigint default(next value for shopping_cart_test_item_id_seq) default on null,
    shopping_cart_id bigint,
    product_id bigint,
    text varchar(1000),
    foreign key(shopping_cart_id) references shopping_cart(id)
);


