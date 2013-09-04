필요한 Framework 및 설정
-------------------------------------

**필요한 Framework**

* SpringMVC
* Atmosphere Framework - https://github.com/Atmosphere/atmosphere
* Twitter/Spring social - https://github.com/SpringSource/spring-social

**목적**

Websockets, Comet을 활용해서 Spring 프레임워크에서 실시간 웹이 가능한지 확인.

** 필요한 설정들 **

* **아래 설정은 Tomcat 7.0.27이상에서는 필요 없다.**

```
<Connector port="8080" protocol="org.apache.coyote.http11.Http11NioProtocol"
               connectionTimeout="600000"
               redirectPort="8443" />
```

tomcat7.0.27에서 웹 소켓은 클라이언트가 커넥션 정보를 잃어버리는 버그가 있어서 connectionTimeout 값을 길게 지정해야 한다.

**기타**

* **AJP protocol은 websockets을 지원하지 않는다.**
* atmosphere에서을 작동시킬려면 아래 context.xml 정보를 META-INF/context.xml에 위치시킨다.


```
<?xml version="1.0" encoding="UTF-8"?>
<Context>
    <Loader delegate="true"/>
</Context>
```

