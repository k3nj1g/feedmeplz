DELETE FROM dishes
WHERE category_id = (SELECT id INTO soup_category_id FROM categories WHERE name = 'Салаты');
