##@SpringBootApplication注解

包含`@SpringBootConfiguration` `@EnableAutoConfiguration` `@ComponentScan`三个注解

###1. `@SpringBootConfiguration`(注解有@Configuration)

    表明@SpringBootConfiguration注解的类是基于Java的配置类
    
###2. `@ComponentScan`

    启用组件扫描，自动扫描controller、service等，与@SpringBootConfiguration配合使用
    
###3. `@EnableAutoConfiguration`

    启用Spring Boot的自动配置功能
    
###4. `REST Api`

    import org.springframework.web.bind.annotation.PathVariable;
    import org.springframework.web.bind.annotation.RequestMapping;
    import org.springframework.web.bind.annotation.RequestMethod;
    import org.springframework.web.bind.annotation.ResponseBody;
    import org.springframework.web.bind.annotation.RestController;
    
    @RestController  //REST
    @RequestMapping("/springboot")
    public class HelloWorldController {
    
        @RequestMapping(value = "/{name}", method = RequestMethod.GET)
        public String sayWorld(@PathVariable("name") String name) {
            return "Hello " + name;
        }
    }
