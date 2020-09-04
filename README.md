# ApiDocBuilder

## 注意事项
插件仅适用于本公司内部框架，其他框架可参考实现原理。

## 思路梳理
 读取源码中的Java doc注释，根据注释读取所有接口信息生成一个json文件，根据json文件的内容可以自由设计接口文档的界面与功能，并可以根据该json文件一键生成所有接口http测试脚本
 
## 实现原理
jdk安装目录下lib文件夹中的tools.jar提供了java doc读取的api，javadoc 在 jdk 目录下只是一个可执行程序，但是这个可执行程序是基于 jdk 的 tools.jar 的一个封装，也就是说 javadoc 实现在 tools.jar 中，借由javadoc可以获取源码几乎所有的信息，类名、类变量、类方法、方法参数、注解、javadoc注释等都可以获取到，然后根据自己的需要把关心的信息输出成自定的格式。

## 使用方法
* 拉取html-dist文件夹内的打包资源文件，并在服务器根目录下创建一个doc文件夹，将拉取资源文件上传到服务器doc目录下，最终文档的访问链接为:http://服务器ip/doc
- 拉取plugin文件夹内的插件jar包，打开idea插件管理界面，选择从磁盘安装，安装完成后重启生效
* 重启后，会在Tools菜单栏出现一个Build ApiDoc的菜单项，点击即可生成文档json文件，生成路径为：项目路径/build/libs/apiList.json，其实就是jar包的打包路径
- 将生成的apiList.json放到服务器doc目录下，在浏览器打开文档链接即可渲染。

## 接口注释规则

### 请求参数


#### 基本用法
```Java
   /**
     * 总后台审核列表
     * @param name          姓名
     * @param clinics       optional:name,auditStatus
     * @param doctor        optional:phone,name
     * @param city          optional:id
     * @param applyTime     optional:start,end
     * @param pageable      required:page,size optional:asc,desc
     * @return
     */
    @Json(
            include = {"id","name","doctor","area","auditStatus","applyTime","manager","auditTime"},
            filter = {
                    @Filter(type = Doctor.class,include = {"id","name","phone"}),
                    @Filter(type = Manager.class,include = {"realname"})
            }
    )
    public List<Clinics> search(String name,Clinics clinics, Doctor doctor,City city, Between<LocalDateTime> applyTime, Pageable pageable) {
      //insert your code
    }
```
* optional:表示为非必填；
- required:表示为必填；
* 非对象字段（例子中的姓名）默认为必填字段，如需设置，可自己进行扩展。

#### 请求参数中，对象有多级的情况
```Java
 /**
     * 用户端加入购物车
     * @param shoppingCart required:goodsSku.id,goodsSku.skuInfo,num
     */
    @RestMethod
    public void addShoppingCart(ShoppingCart shoppingCart){
      //insert your code
    }
```

上述代码中，shoppingCart里面有个对象为goodsSku，需要传shoppingCart里面的goodsSku对象,并只传里面的id和skuInfo两个字段，直接用“.”连接，如果为List类型，则直接以泛型对象为准，例如：

```Java
 /**
     * 用户端加入购物车
     * @param shoppingCartList required:goodsSku.id,num
     */
    @RestMethod
    public void addShoppingCart(List<ShoppingCart> shoppingCartList){
      //insert your code
    }
```
对于多级的情况，最好不要超过两级，能不使用最好不使用，因为对前端的同事不太友好。

### 返回结果

#### 基本用法

基本用法就是跟平时开发一样写好@Json就行，最好不要省略@Json注解，@Json注解中的include或者exclude也建议不要省略，有利于生成更清晰明了的接口文档。

#### 返回结果中有循环引用的情况
```Java
 /**
     * 用户端获取常见病症和常见科室
     * @return
     */
    @RestMethod
    @Json(
            filter={
              @Filter(type=ClinicalDepartment.class,include = {"id","name","image","commonDepartment","commonDisease","detail"})
            }
    )
    public ClinicalDepartment getAllClinicalDepartment(){
        //insert your code
    }
```
上述代码中，ClinicalDepartment类里面包含了commonDepartment与commonDisease两个字段，这两个字段正好也是ClinicalDepartment类型，这种情况就会出现循环引用的情况，如不做处理会导致递归死循环，最终导致StackOverflow，目前插件已经解决了此类循环引用的问题，但最好还是不要使用这种写法。

### 最后的话

* 该插件仅适用于本公司内部框架，其他框架可参考实现原理。
- 由于手头上项目非常紧急，时间仓促，前端大佬都在等着我的接口文档开饭，所以插件代码质量有点差，但是bug基本都解决了，第一个使用它的项目有七百多个接口，基本上能应付大部分场景，有时间我会进行重构优化。

### 参考资料

https://www.jianshu.com/p/d5c2781d3070

https://blog.csdn.net/baiihcy/article/details/53861267

https://gitee.com/l0km/javadocreader

https://jetbrains.org/intellij/sdk/docs/intro/welcome.html


