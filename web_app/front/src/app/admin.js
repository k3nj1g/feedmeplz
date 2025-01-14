import React, { useState } from 'react';
import { 
  Calendar, Clock, User, Users, Settings, ShoppingCart, 
  Edit, Trash2, Plus, ChevronDown, Image
} from 'lucide-react';
import {
  Card,
  CardContent,
  CardHeader,
  CardTitle,
} from "@/components/ui/card";

const DishCatalog = () => {
  const [openCategory, setOpenCategory] = useState('Салаты');

  const categories = [
    'Салаты',
    'Супы',
    'Горячие блюда',
    'Гарниры',
    'Десерты'
  ];

  const dishes = {
    'Салаты': [
      { id: 1, name: 'Цезарь с курицей', description: 'Салат романо, куриное филе, гренки, пармезан, соус цезарь', price: 420, weight: 220 },
      { id: 2, name: 'Греческий', description: 'Свежие овощи, маслины, сыр фета, оливковое масло', price: 380, weight: 200 }
    ],
    'Супы': [
      { id: 3, name: 'Борщ', description: 'Традиционный борщ со сметаной', price: 320, weight: 300 },
      { id: 4, name: 'Куриный суп', description: 'Куриный бульон с лапшой и овощами', price: 280, weight: 300 }
    ],
    // другие категории с блюдами
  };

  const renderDishEditor = (dish) => (
    <Card key={dish.id} className="bg-white">
      <CardContent className="p-4">
        <div className="flex items-start">
          <div className="w-24 h-24 bg-gray-100 rounded-md flex items-center justify-center mr-4">
            <Image className="w-8 h-8 text-gray-400" />
          </div>
          <div className="flex-1">
            <div className="flex justify-between items-start">
              <div>
                <input
                  type="text"
                  value={dish.name}
                  className="font-medium text-lg mb-1 border-b border-transparent hover:border-gray-300 focus:border-blue-500 focus:outline-none"
                />
                <textarea
                  value={dish.description}
                  className="text-sm text-gray-600 w-full mt-1 border-b border-transparent hover:border-gray-300 focus:border-blue-500 focus:outline-none"
                  rows={2}
                />
              </div>
              <div className="flex space-x-2">
                <button className="p-2 text-gray-500 hover:text-blue-500">
                  <Edit className="w-4 h-4" />
                </button>
                <button className="p-2 text-gray-500 hover:text-red-500">
                  <Trash2 className="w-4 h-4" />
                </button>
              </div>
            </div>
            <div className="flex space-x-4 mt-2">
              <div className="flex items-center">
                <span className="text-sm text-gray-500 mr-2">Цена:</span>
                <input
                  type="number"
                  value={dish.price}
                  className="w-20 border rounded-md px-2 py-1"
                />
                <span className="text-sm text-gray-500 ml-1">₽</span>
              </div>
              <div className="flex items-center">
                <span className="text-sm text-gray-500 mr-2">Вес:</span>
                <input
                  type="number"
                  value={dish.weight}
                  className="w-20 border rounded-md px-2 py-1"
                />
                <span className="text-sm text-gray-500 ml-1">г</span>
              </div>
            </div>
          </div>
        </div>
      </CardContent>
    </Card>
  );

  return (
    <div className="max-w-6xl mx-auto px-4 py-6">
      <div className="flex justify-between items-center mb-6">
        <h1 className="text-2xl font-semibold">Каталог блюд</h1>
        <button className="flex items-center px-4 py-2 bg-blue-500 text-white rounded-md hover:bg-blue-600">
          <Plus className="w-4 h-4 mr-2" />
          Добавить блюдо
        </button>
      </div>

      <div className="flex">
        <div className="w-64 pr-6">
          <div className="bg-white rounded-lg shadow">
            {categories.map(category => (
              <button
                key={category}
                className={`w-full text-left px-4 py-3 hover:bg-gray-50 ${
                  openCategory === category ? 'bg-blue-50 text-blue-600' : 'text-gray-700'
                }`}
                onClick={() => setOpenCategory(category)}
              >
                {category}
              </button>
            ))}
          </div>
        </div>

        <div className="flex-1 space-y-4">
          <div className="flex justify-between items-center mb-4">
            <h2 className="text-xl font-medium">{openCategory}</h2>
          </div>
          {dishes[openCategory]?.map(dish => renderDishEditor(dish))}
        </div>
      </div>
    </div>
  );
};

const AdminPanel = () => {
  const [activeSection, setActiveSection] = useState('menu-list');
  const [showAdminMenu, setShowAdminMenu] = useState(false);

  return (
    <div>
      {/* Основная навигация */}
      <nav className="bg-white shadow-sm">
        <div className="max-w-6xl mx-auto px-4">
          <div className="flex justify-between h-16">
            <div className="flex space-x-8">
              <span className="flex items-center text-xl font-semibold text-blue-600">
                Office Lunches
              </span>
              
              <button 
                className={`inline-flex items-center px-1 pt-1 border-b-2 ${
                  activeSection === 'menu' ? 'border-blue-500 text-gray-900' : 'border-transparent text-gray-500'
                }`}
                onClick={() => setActiveSection('menu')}
              >
                <Clock className="w-5 h-5 mr-2" />
                Меню дня
              </button>
              
              <button 
                className={`inline-flex items-center px-1 pt-1 border-b-2 ${
                  activeSection === 'orders' ? 'border-blue-500 text-gray-900' : 'border-transparent text-gray-500'
                }`}
                onClick={() => setActiveSection('orders')}
              >
                <ShoppingCart className="w-5 h-5 mr-2" />
                Мои заказы
              </button>
              
              <div className="relative">
                <button 
                  className={`inline-flex items-center px-1 pt-1 border-b-2 ${
                    activeSection.startsWith('admin') ? 'border-blue-500 text-gray-900' : 'border-transparent text-gray-500'
                  }`}
                  onClick={() => setShowAdminMenu(!showAdminMenu)}
                >
                  <Settings className="w-5 h-5 mr-2" />
                  Управление
                  <ChevronDown className="w-4 h-4 ml-1" />
                </button>

                {showAdminMenu && (
                  <div className="absolute z-10 left-0 mt-2 w-56 rounded-md shadow-lg bg-white ring-1 ring-black ring-opacity-5">
                    <div className="py-1">
                      <button
                        className="block px-4 py-2 text-sm text-gray-700 hover:bg-gray-100 w-full text-left"
                        onClick={() => {
                          setActiveSection('admin-catalog');
                          setShowAdminMenu(false);
                        }}
                      >
                        Каталог блюд
                      </button>
                      <button
                        className="block px-4 py-2 text-sm text-gray-700 hover:bg-gray-100 w-full text-left"
                        onClick={() => {
                          setActiveSection('admin-daily');
                          setShowAdminMenu(false);
                        }}
                      >
                        Меню дня
                      </button>
                      <button
                        className="block px-4 py-2 text-sm text-gray-700 hover:bg-gray-100 w-full text-left"
                        onClick={() => {
                          setActiveSection('admin-users');
                          setShowAdminMenu(false);
                        }}
                      >
                        Пользователи
                      </button>
                      <button
                        className="block px-4 py-2 text-sm text-gray-700 hover:bg-gray-100 w-full text-left"
                        onClick={() => {
                          setActiveSection('admin-roles');
                          setShowAdminMenu(false);
                        }}
                      >
                        Роли
                      </button>
                    </div>
                  </div>
                )}
              </div>
            </div>
            
            <div className="flex items-center space-x-4">
              <User className="w-5 h-5 text-gray-500" />
              <span className="text-gray-700">Иван Петров</span>
            </div>
          </div>
        </div>
      </nav>

      {/* Основной контент */}
      <main>
        {activeSection === 'admin-catalog' && <DishCatalog />}
        {/* Другие разделы... */}
      </main>
    </div>
  );
};

export default AdminPanel;
