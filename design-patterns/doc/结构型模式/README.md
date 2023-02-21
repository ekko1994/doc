# 结构型模式（Structural Patterns）

- 结构型模式关注点“怎样组合对象/类？”所以我们关注下类的组合关系
- 类结构型模式关心类的组合，由多个类可以组合成一个更大的（继承）
- 对象结构型模式关心类与对象的组合，通过关联关系在一个类中定义另一个类的实例对象（组合）
- 根据“合成复用原则”，在系统中尽量使用关联关系来替代继承关系，因此大部分结构型模式都是对象结构型模式。
  - **适配器模式（Adapter Pattern）**：两个不兼容接口之间适配的桥梁
  - 桥接模式（Bridge Pattern）：相同功能抽象化与实现化解耦，抽象与实现可以独立升级。
  - 过滤器模式（Filter、Criteria Pattern）：使用不同的标准来过滤一组对象
  - **组合模式（Composite Pattern）**：相似对象进行组合，形成树形结构
  - **装饰器模式（Decorator Pattern）**：向一个现有的对象添加新的功能，同时又不改变其结构
  - **外观模式（Facade Pattern）**：向现有的系统添加一个接口，客户端访问此接口来隐藏系统的复杂性。
  - 享元模式（Flyweight Pattern）：尝试重用现有的同类对象，如果未找到匹配的对象，则创建新对象
  - **代理模式（Proxy Pattern）**：一个类代表另一个类的功能

## 1. 适配器模式（Adapter Pattern）

- 将**一个接口转换成客户希望的另一个接口**，适配器模式使接口不兼容的那些类可以一起工作，适配器模式分为类结构型模式（继承）和对象结构型模式（组合）两种，前者（继承）类之间的耦合度比后者高，且要求程序员了解现有组件库中的相关组件的内部结构，所以应用相对较少些。
- 别名也可以是Wrapper，包装器

适配器模式（Adapter）包含以下主要角色。
目标（Target）接口：可以是抽象类或接口。客户希望直接用的接口
适配者（Adaptee）类：隐藏的转换接口
适配器（Adapter）类：它是一个转换器，通过继承或引用适配者的对象，把适配者接口转换成目标接口。

![image-20230221124225275](images/image-20230221124225275.png)

类结构型：

![image-20230221124246794](images/image-20230221124246794.png)

对象结构型：

![image-20230221124343431](images/image-20230221124343431.png)

**什么场景用到？**

- Tomcat如何将Request流转为标准Request；
  - tomcat.Request接口
  - servlet.Request接口
  - tomcat ===  CoyoteAdapte === ServletRequest
- Spring AOP中的AdvisorAdapter是什么：增强的适配器
  - 前置、后置、返回、结束  Advisor（通知方法）
  - 底层真的目标方法
- Spring MVC中经典的HandlerAdapter是什么；
  - HelloController.hello()
  - HandlerAdapter
  - Servlet.doGet()
- SpringBoot 中 WebMvcConfigurerAdapter为什么存在又取消
  ......

## 2. 原型（Prototype）模式



## 3. 工厂（Factory）模式

## 4. 建造者（Builder）模式

