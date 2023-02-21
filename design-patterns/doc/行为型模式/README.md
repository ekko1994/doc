# 行为型模式（Behavioral Patterns）

- 行为型模式关注点“**怎样运行对象/类？**”所以我们关注下类/对象的运行时流程控制
- 行为型模式用于描述程序在运行时复杂的流程控制，
- 描述多个类或对象之间怎样相互协作共同完成单个对象都无法单独完成的任务，它涉及算法与对象间职责的分配。
- 行为型模式分为**类行为模式和对象行为模式**，前者采用继承机制来在类间分派行为，后者采用组合或聚合在对象间分配行为。由于组合关系或聚合关系比继承关系耦合度低，满足“**合成复用原则**”，所以对象行为模式比类行为模式具有更大的灵活性。



- **模板方法（Template Method）模式：父类定义算法骨架，某些实现放在子类**
- **策略（Strategy）模式：每种算法独立封装，根据不同情况使用不同算法策略**
- **状态（State）模式：每种状态独立封装，不同状态内部封装了不同行为**
- 命令（Command）模式：将一个请求封装为一个对象，使发出请求的责任和执行请求的责任分割开
- **职责链（Chain of Responsibility）模式：所有处理者封装为链式结构，依次调用**
- 备忘录（Memento）模式：把核心信息抽取出来，可以进行保存
- 解释器（Interpreter）模式：定义语法解析规则
- **观察者（Observer）模式：维护多个观察者依赖，状态变化通知所有观察者**
- 中介者（Mediator）模式：取消类/对象的直接调用关系，使用中介者维护
- 迭代器（Iterator）模式：定义集合数据的遍历规则
- 访问者（Visitor）模式：分离对象结构，与元素的执行算法

**除了模板方法模式和解释器模式是类行为型模式，其他的全部属于对象行为型模式**

## 1. 模板方法（Template Method）

- 在模板模式（Template Pattern）中，一个抽象类公开定义了执行它的方法的方式模板。它的子类可以按需要重写方法实现，但调用将以抽象类中定义的方式进行。

![image-20230221171930471](images/image-20230221171930471.png)

模板方法（Template Method）包含两个角色

抽象类/抽象模板（Abstract Class）

具体子类/具体实现（Concrete Class）

什么场景用到？

- Spring的整个继承体系都基本用到模板方法;
  - BeanFactory.getBean(1,2,3,4)--A1---A2---A3---A4（全部被完成）
- JdbcTemplate、RedisTemplate都允许我们再扩展.....
- 我们自己的系统也应该使用模板方法组织类结构
- ......

## 2. 桥接模式（Bridge Pattern）

- **将抽象与实现解耦，使两者都可以独立变化**
- 在现实生活中，某些类具有两个或多个维度的变化，如图形既可按形状分，又可按颜色分。如何设计类似于 Photoshop 这样的软件，能画不同形状和不同颜色的图形呢？如果用继承方式，m 种形状和 n 种颜色的图形就有 m×n 种，不但对应的子类很多，而且扩展困难。不同颜色和字体的文字、不同品牌和功率的汽车
- **桥接将继承转为关联，降低类之间的耦合度，减少代码量**

![image-20230221141154174](images/image-20230221141154174.png)

桥接（Bridge）模式包含以下主要角色：

- 系统设计期间，如果这个类里面的一些东西，会扩展很多，这个东西就应该分离出来
- 抽象化（Abstraction）角色：定义抽象类，并包含一个对实现化对象的引用。
- 扩展抽象化（Refined Abstraction）角色：是抽象化角色的子类，实现父类中的业务方法，并通过组合关系调用实现化角色中的业务方法。
- 实现化（Implementor）角色：定义实现化角色的接口，供扩展抽象化角色调用。

![image-20230221141246276](images/image-20230221141246276.png)

什么场景用到？

- 当一个类存在两个独立变化的维度，且这两个维度都需要进行扩展时。
- 当一个系统不希望使用继承或因为多层次继承导致系统类的个数急剧增加时。
- 当一个系统需要在构件的抽象化角色和具体化角色之间增加更多的灵活性时。
- InputStreamReader桥接模式。An InputStreamReader is a bridge from byte streams to character streams:
- InputStreamReader 桥接+适配器

## 3. 装饰器模式（Decorator/Wrapper（包装） Pattern）

- **适配器是连接两个类，可以增强一个类，装饰器是增强一个类**
- 向一个现有的对象添加新的功能，同时又不改变其结构。属于对象结构型模式。
- 创建了一个装饰类，用来包装原有的类，并在保持类方法签名完整性的前提下，提供了额外的功能。

![image-20230221144859786](images/image-20230221144859786.png)

抽象构件（Component）角色：
		定义一个抽象接口以规范准备接收附加责任的对象。
具体构件（ConcreteComponent）角色：
		实现抽象构件，通过装饰角色为其添加一些职责。
抽象装饰（Decorator）角色：
		继承抽象构件，并包含具体构件的实例，可以通过其子类扩展具体构件的功能。
具体装饰（ConcreteDecorator）角色：
		实现抽象装饰的相关方法，并给具体构件对象添加附加的责任。



什么场景使用？

- 无处不在.....
- SpringSession中如何进行session与redis关联？HttpRequestWrapper
  - session：数据存在了内存
  - session：数据存在redis
  - HttpSession；getAttribute();
  - Wrapper(session){
    - getAttribute(String param){    redis.get(param) };
  - }
- MyBatisPlus提取了QueryWrapper，这是什么？
- Spring中的BeanWrapper是做什么？包装了Bean。bean的功能增强？
- Spring Webflux中的 WebHandlerDecorator？
- 已存的类，每一天在某个功能使用的时候发现不够，就可以装饰器。
- ......

## 4. 代理模式（Proxy Pattern）

代理模式(Proxy Pattern) ,给某一个对象提供一个代理，并由代理对象控制对原对象的引用,对象结构型模式。这种也是静态代理

![image-20230221151604450](images/image-20230221151604450.png)

代理模式包含如下角色：

Subject: 抽象主体角色(抽象类或接口)

Proxy: 代理主体角色(代理对象类)

RealSubject: 真实主体角色(被代理对象类)

**什么场景用到？**

- MyBatis的mapper到底是什么？怎么生成的？
- 动态代理
  - UserMapper、CityMapper，mybatis帮我们写实现MapperProxy
- Alibaba Seata的DataSourceProxy是什么？
- DruidDataSource存在的Proxy模式
  - 监控链...
- ......

## 5. 外观模式（Facade Pattern）

外观（Facade）模式又叫作门面模式，是一种通过为多个复杂的子系统提供一个一致的接口，而使这些子系统更加容易被访问的模式

![image-20230221160823363](images/image-20230221160823363.png)

什么场景使用？

- 去医院看病，可能要去挂号、门诊、划价、取药，让患者或患者家属觉得很复杂，如果有提供接待人员，只让接待人员来处理，就很方便。以此类比......
- JAVA 的三层开发模式。
- 分布式系统的网关
- Tomcat源码中的RequestFacade干什么的？
- ......

## 6. 组合模式（Composite Pattern）

把一组相似的对象当作一个单一的对象。如：树形菜单

![image-20230221162053128](images/image-20230221162053128.png)什么场景用到？

- 层级结构

- 部门组织结构

- 组合了别的对象还是组合模式吗？

- ......

## 7. 享元模式（Flyweight Pattern）

- 享元模式(Flyweight Pattern)，运用共享技术有效地支持大量细粒度对象的复用。系统只使用少量的对象，而这些对象都很相似，状态变化很小，可以实现对象的多次复用。对象结构型
- 在享元模式中**可以共享的相同内容称为内部状态(IntrinsicState)**，而那些需要外部环境来设置的**不能共享的内容称为外部状态(Extrinsic State)**，由于区分了内部状态和外部状态，因此可以通过设置不同的外部状态使得相同的对象可以具有一些不同的特征，而相同的内部状态是可以共享的。
- 在享元模式中通常会出现工厂模式，需要创建一个**享元工厂来负责维护一个享元池**(Flyweight Pool)用于存储具有相同内部状态的享元对象。
- 池

享元模式包含如下角色：
Flyweight: 抽象享元类  Connection
ConcreteFlyweight: 具体享元类  ConnectionImpl（user,pwd,url）
UnsharedConcreteFlyweight: 非共享具体享元类ConnectionImpl（state）
FlyweightFactory: 享元工厂类；简单工厂，产品就一个Connection

![image-20230221164739983](images/image-20230221164739983.png)

什么场景用到？

- 典型的代表：数据库连接池
- 所有的池化技术
- 享元和原型模式有什么区别？享元是预先准备好的对象进行复用，原型没法确定预先有哪些
- ......
