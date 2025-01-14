import React, { useState } from 'react';
import { Calendar, Clock, User, Users, Settings, ShoppingCart } from 'lucide-react';
import {
  Card,
  CardContent,
  CardHeader,
  CardTitle,
} from "@/components/ui/card";

const LunchOrderSystem = () => {
  const [activeTab, setActiveTab] = useState('menu');
  
  // Примеры данных
  const menuItems = [
    {
      id: 1,
      name: 'Борщ украинский',
      description: 'Свекла, капуста, морковь, картофель, говядина',
      price: 350,
      category: 'Супы'
    },
    {
      id: 2,
      name: 'Котлета куриная',
      description: 'Куриное филе, специи, панировка',
      price: 280,
      category: 'Горячие блюда'
    }
  ];

  return (
    <div className="min-h-screen bg-gray-50">
      {/* Верхняя панель навигации */}
      <nav className="bg-white shadow-sm">
        <div className="max-w-6xl mx-auto px-4">
          <div className="flex justify-between h-16">
            <div className="flex space-x-8">
              <button 
                className={`inline-flex items-center px-1 pt-1 border-b-2 ${
                  activeTab === 'menu' 
                    ? 'border-blue-500 text-gray-900' 
                    : 'border-transparent text-gray-500'
                }`}
                onClick={() => setActiveTab('menu')}
              >
                <Clock className="w-5 h-5 mr-2" />
                Меню дня
              </button>
              
              <button 
                className={`inline-flex items-center px-1 pt-1 border-b-2 ${
                  activeTab === 'orders' 
                    ? 'border-blue-500 text-gray-900' 
                    : 'border-transparent text-gray-500'
                }`}
                onClick={() => setActiveTab('orders')}
              >
                <ShoppingCart className="w-5 h-5 mr-2" />
                Мои заказы
              </button>
              
              <button 
                className={`inline-flex items-center px-1 pt-1 border-b-2 ${
                  activeTab === 'admin' 
                    ? 'border-blue-500 text-gray-900' 
                    : 'border-transparent text-gray-500'
                }`}
                onClick={() => setActiveTab('admin')}
              >
                <Settings className="w-5 h-5 mr-2" />
                Управление
              </button>
            </div>
            
            <div className="flex items-center">
              <User className="w-5 h-5 text-gray-500" />
              <span className="ml-2 text-gray-700">Иван Петров</span>
            </div>
          </div>
        </div>
      </nav>

      {/* Основной контент */}
      <main className="max-w-6xl mx-auto px-4 py-6">
        {activeTab === 'menu' && (
          <div className="space-y-6">
            <div className="flex justify-between items-center">
              <h1 className="text-2xl font-semibold text-gray-900">Меню на сегодня</h1>
              <div className="flex items-center space-x-4">
                <Calendar className="w-5 h-5 text-gray-500" />
                <span className="text-gray-700">14 января 2025</span>
              </div>
            </div>

            <div className="grid grid-cols-1 md:grid-cols-2 gap-6">
              {menuItems.map((item) => (
                <Card key={item.id} className="bg-white">
                  <CardHeader>
                    <CardTitle className="text-lg">{item.name}</CardTitle>
                  </CardHeader>
                  <CardContent>
                    <p className="text-gray-600 text-sm mb-4">{item.description}</p>
                    <div className="flex justify-between items-center">
                      <span className="text-gray-900 font-medium">{item.price} ₽</span>
                      <div className="flex items-center space-x-2">
                        <button className="p-1 text-gray-500 hover:text-gray-700">-</button>
                        <span className="w-8 text-center">0</span>
                        <button className="p-1 text-gray-500 hover:text-gray-700">+</button>
                      </div>
                    </div>
                  </CardContent>
                </Card>
              ))}
            </div>

            <div className="fixed bottom-0 left-0 right-0 bg-white shadow-lg border-t">
              <div className="max-w-6xl mx-auto px-4 py-4 flex justify-between items-center">
                <div>
                  <span className="text-gray-600">Итого:</span>
                  <span className="ml-2 text-lg font-semibold">0 ₽</span>
                </div>
                <button className="bg-blue-500 text-white px-6 py-2 rounded-md hover:bg-blue-600">
                  Оформить заказ
                </button>
              </div>
            </div>
          </div>
        )}

        {activeTab === 'orders' && (
          <div className="space-y-6">
            <h1 className="text-2xl font-semibold text-gray-900">История заказов</h1>
            {/* Здесь будет компонент истории заказов */}
          </div>
        )}

        {activeTab === 'admin' && (
          <div className="space-y-6">
            <h1 className="text-2xl font-semibold text-gray-900">Панель управления</h1>
            {/* Здесь будет админ-панель */}
          </div>
        )}
      </main>
    </div>
  );
};

export default LunchOrderSystem;
