set mode Oracle;
set schema public;

insert into product_category (id, name) values (product_category_id_seq.nextval, 'Desktop');
insert into product_category (id, name) values (product_category_id_seq.nextval, 'Laptop');
insert into product_category (id, name) values (product_category_id_seq.nextval, 'Peripheral');

--insert into product (id, name, description, price, category_id) values (product_id_seq.nextval, 'MacBook Pro', 'Apple Macbook Pro', 11000.0, 2);
--insert into product (id, name, description, price, category_id) values (product_id_seq.nextval, 'Dell Desktop', 'Dell Desktop i5', 5000.0, 1);
--insert into product (id, name, description, price, category_id) values (product_id_seq.nextval, 'Mechanical Keyboard', 'Mechanical Keyboard Clicky', 1678.0, 3);
--insert into product (id, name, description, price, category_id) values (product_id_seq.nextval, 'iPad Pro', 'iPad Pro', 8000.0, 3);
--insert into product (id, name, description, price, category_id) values (product_id_seq.nextval, 'Bluetooth mouse', 'Logitech bluetooth mouse', 1598.0, 3);


--insert into discount_rule (id, threshold_unit, threshold_unit_type, applicable_unit, applicable_unit_type, applicable_discount, override_amount, rule_group_id, description)
--    values(1, 2, 'Qty', 1, 'Qty', 0.5, 0.0, 1, 'Buy 1 get 50% off the second');

--insert into discount_rule (id, threshold_unit, threshold_unit_type, applicable_unit, applicable_unit_type, applicable_discount, override_amount, rule_group_id, description)
--    values(2, 3, 'Qty', 3, 'Qty', 0.6, 0.0, 1, 'Buy 3 get 60% off all 3');

--insert into discount_rule (id, threshold_unit, threshold_unit_type, applicable_unit, applicable_unit_type, applicable_discount, override_amount, rule_group_id, description)
--    values(3, 5, 'Qty', 5, 'Qty', 0.7, 0.0, 1, 'Buy 3 get 70% off all 5');

--insert into discount_rule_setting (id, category_id, product_id, rule_group_id)
--    values(1, 1, null, 1);

--insert into discount_rule_setting (id, category_id, product_id, rule_group_id)
--    values(2, 2, null, 1);

--insert into discount_rule_setting (id, category_id, product_id, rule_group_id)
--    values(3, null, 5, 1);

--insert into discount_rule_setting (id, category_id, product_id, rule_group_id)
--    values(4, null, 4, 1);
