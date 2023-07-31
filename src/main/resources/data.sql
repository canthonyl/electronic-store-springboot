set mode Oracle;
set schema public;

insert into product_category (id, name) values (product_category_id_seq.nextval, 'Desktop');
insert into product_category (id, name) values (product_category_id_seq.nextval, 'Laptop');
insert into product_category (id, name) values (product_category_id_seq.nextval, 'Peripheral');

--insert into product (id, name, description, price, category_id) values (product_id_seq.nextval, 'MacBook Pro', 'Apple Macbook Pro', 11000.0, 2);
--insert into product (id, name, description, price, category_id) values (product_id_seq.nextval, 'Dell Desktop', 'Dell Desktop i5', 5000.0, 1);
--insert into product (id, name, description, price, category_id) values (product_id_seq.nextval, 'Mechanical Keyboard', 'Mechanical Keyboard Clicky', 600.0, 3);
