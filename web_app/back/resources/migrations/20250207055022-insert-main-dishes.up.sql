DO $$
DECLARE
    main_dish_category_id INT;
BEGIN
    SELECT id INTO main_dish_category_id FROM categories WHERE name = 'Горячие блюда';
    INSERT INTO dishes (name, price, category_id) VALUES
    ('Азу со свининой', 125, main_dish_category_id),
    ('Бедро куриное запеченное', 100, main_dish_category_id),
    ('Бефстроганов', 120, main_dish_category_id),
    ('Биточек', 80, main_dish_category_id),
    ('Биточек куриный', 100, main_dish_category_id),
    ('Бифштекс с яйцом', 100, main_dish_category_id),
    ('Голень куриная', 100, main_dish_category_id),
    ('Голубцы ленивые', 80, main_dish_category_id),
    ('Горбуша жаренная', 90, main_dish_category_id),
    ('Горбуша припущенная с овощами', 90, main_dish_category_id),
    ('Гуляш свиной', 120, main_dish_category_id),
    ('Ежики куриные', 95, main_dish_category_id),
    ('Ежики мясные', 80, main_dish_category_id),
    ('Жаркое из курицы', 130, main_dish_category_id),
    ('Жаркое со свининой', 135, main_dish_category_id),
    ('Запеканка картофельная с мясом', 130, main_dish_category_id),
    ('Зразы куриные', 100, main_dish_category_id),
    ('Зразы куриные с яйцом и луком', 85, main_dish_category_id),
    ('Зразы с грибами и сыром', 100, main_dish_category_id),
    ('Зразы с зелёным луком и яйцом', 95, main_dish_category_id),
    ('Зразы с луком и яйцом', 95, main_dish_category_id),
    ('Зразы с луком из куриного филе', 95, main_dish_category_id),
    ('Зразы с сыром', 85, main_dish_category_id),
    ('Зразы с шампиньонами', 100, main_dish_category_id),
    ('Зразы с шампиньонами и сыром', 100, main_dish_category_id),
    ('Картофельная запеканка', 130, main_dish_category_id),
    ('Картофельная запеканка с мясом', 125, main_dish_category_id),
    ('Кнели куриные', 95, main_dish_category_id),
    ('Котлета домашняя', 90, main_dish_category_id),
    ('Котлета запеченная с грибами', 85, main_dish_category_id),
    ('Котлета запеченная с маслинами', 85, main_dish_category_id),
    ('Котлета запеченная с оливками', 85, main_dish_category_id),
    ('Котлета запеченная с сыром', 85, main_dish_category_id),
    ('Котлета запеченная с сыром и томатом', 85, main_dish_category_id),
    ('Котлета запеченная с томатом', 85, main_dish_category_id),
    ('Котлета запеченная с черносливом', 95, main_dish_category_id),
    ('Котлета запеченная с шампиньонами', 95, main_dish_category_id),
    ('Котлета из куриного филе', 85, main_dish_category_id),
    ('Котлета куриная домашняя', 90, main_dish_category_id),
    ('Котлета куриная запеченная с оливками', 85, main_dish_category_id),
    ('Котлета куриная запеченная с сыром', 95, main_dish_category_id),
    ('Котлета куриная запеченная с шампиньонами', 95, main_dish_category_id),
    ('Котлета куриная пикантная', 95, main_dish_category_id),
    ('Котлета куриная с болгарский перцем и сыром', 85, main_dish_category_id),
    ('Котлета по-крымски', 90, main_dish_category_id),
    ('Котлета полтавская', 80, main_dish_category_id),
    ('Крылья куриные BBQ', 95, main_dish_category_id),
    ('Купаты', 80, main_dish_category_id),
    ('Купаты куриные', 100, main_dish_category_id),
    ('Куриная поджарка', 120, main_dish_category_id),
    ('Куриная поджарка в сливочном соусе', 120, main_dish_category_id),
    ('Куриное соте на шпажке', 99, main_dish_category_id),
    ('Куриное филе в сливочно-горчичном соусе', 120, main_dish_category_id),
    ('Куриное филе в сливочно-грибном соусе', 120, main_dish_category_id),
    ('Куриное филе запеченное под сыром', 120, main_dish_category_id),
    ('Куриное филе запеченное с ананасом', 120, main_dish_category_id),
    ('Куриное филе запеченное с томатом', 120, main_dish_category_id),
    ('Куриное филе запеченное с томатом и сыром', 120, main_dish_category_id),
    ('Куриное филе запеченное с черносливом', 120, main_dish_category_id),
    ('Куриное филе запеченное с черносливом и сыром', 120, main_dish_category_id),
    ('Куриное филе запеченное с шампиньонами', 120, main_dish_category_id),
    ('Куриное филе запеченное с шампиньонами и сыром', 125, main_dish_category_id),
    ('Куриный рулет с овощами', 85, main_dish_category_id),
    ('Куриный рулет с сыром и зеленью', 95, main_dish_category_id),
    ('Куриный рулет с творогом зеленью и чесноком', 120, main_dish_category_id),
    ('Куриный рулет с черносливом', 99, main_dish_category_id),
    ('Куриный рулет с черносливом и зеленью', 99, main_dish_category_id),
    ('Куриный стейк', 120, main_dish_category_id),
    ('Куриный стейк запеченный с сыром', 99, main_dish_category_id),
    ('Люля-кебаб', 95, main_dish_category_id),
    ('Люля-кебаб куриные', 110, main_dish_category_id),
    ('Макароны по-флотски', 115, main_dish_category_id),
    ('Мафины куриные с грибами', 95, main_dish_category_id),
    ('Минтай жаренный', 90, main_dish_category_id),
    ('Морской язык жаренный', 90, main_dish_category_id),
    ('Мясной рулет с грибами и сыром', 130, main_dish_category_id),
    ('Мясной рулет с сыром и зеленью', 85, main_dish_category_id),
    ('Мясо жареное с розмарином', 99, main_dish_category_id),
    ('Мясо запеченное в маринаде', 99, main_dish_category_id),
    ('Мясо запеченное под сыром', 120, main_dish_category_id),
    ('Мясо запеченное с маслинами и оливками', 99, main_dish_category_id),
    ('Мясо по-купечески с грибами', 120, main_dish_category_id),
    ('Мясо по-провански', 99, main_dish_category_id),
    ('Мясо по-французски', 120, main_dish_category_id),
    ('Печень куриная тушеная с овощами', 120, main_dish_category_id),
    ('Плов', 135, main_dish_category_id),
    ('Плов с курицей', 135, main_dish_category_id),
    ('Плов со свининой', 135, main_dish_category_id),
    ('Поджарка из куриного филе', 120, main_dish_category_id),
    ('Поджарка куриная', 120, main_dish_category_id),
    ('Поджарка свиная', 120, main_dish_category_id),
    ('Рагу с курицей', 135, main_dish_category_id),
    ('Рагу с курицей и овощами', 135, main_dish_category_id),
    ('Рагу с мясом', 130, main_dish_category_id),
    ('Рагу с мясом и сезонными овощами', 125, main_dish_category_id),
    ('Ребра свиные BBQ', 95, main_dish_category_id),
    ('Рулет куриный с творогом, зеленью и чесноком', 105, main_dish_category_id),
    ('Рулет с грибами и сыром', 120, main_dish_category_id),
    ('Свинина в кисло-сладком соусе', 120, main_dish_category_id),
    ('Свинина в сливочно-горчичном соусе', 120, main_dish_category_id),
    ('Свинина в сливочно-грибном соусе', 120, main_dish_category_id),
    ('Свинина жаренная', 99, main_dish_category_id),
    ('Свинина отбивная', 99, main_dish_category_id),
    ('Свинина с грибами в томатном соусе', 99, main_dish_category_id),
    ('Свинина с овощами тушенная', 99, main_dish_category_id),
    ('Свиной гуляш', 99, main_dish_category_id),
    ('Свиные стейки в сливочно-грибном маринаде', 99, main_dish_category_id),
    ('Сосиски отварные', 65, main_dish_category_id),
    ('Стейк куриный', 120, main_dish_category_id),
    ('Стейк куриный в сливочно-грибном маринаде', 120, main_dish_category_id),
    ('Стейк куриный в сметане', 120, main_dish_category_id),
    ('Стейк куриный маринованный в сметане', 120, main_dish_category_id),
    ('Стейк куриный с имбирем и медом', 120, main_dish_category_id),
    ('Стейк куриный с сыром', 120, main_dish_category_id),
    ('Тефтели в сливочном соусе', 85, main_dish_category_id),
    ('Тефтели в томатном соусе', 80, main_dish_category_id),
    ('Тефтели куриные', 85, main_dish_category_id),
    ('Тефтели куриные в сливочном соусе', 95, main_dish_category_id),
    ('Тефтели куриные в томатном соусе', 100, main_dish_category_id),
    ('Треска жаренная', 100, main_dish_category_id),
    ('Удон с курицей', 130, main_dish_category_id),
    ('Удон со свининой', 135, main_dish_category_id),
    ('Филе морского языка жареное', 90, main_dish_category_id),
    ('Форель белая жареная', 100, main_dish_category_id),
    ('Форель белая припущенная с овощами', 90, main_dish_category_id),
    ('Фрикадельки в сливочном соусе', 85, main_dish_category_id),
    ('Фрикадельки куриные', 85, main_dish_category_id),
    ('Фрикадельки куриные в сливочном соусе', 85, main_dish_category_id),
    ('Фрикадельки куриные в томатном соусе', 95, main_dish_category_id),
    ('Шашлычки свиные', 99, main_dish_category_id),
    ('Шницель', 95, main_dish_category_id),
    ('Шницель куриный', 100, main_dish_category_id),
    ('Шницель свиной натуральный', 120, main_dish_category_id);
END $$;
