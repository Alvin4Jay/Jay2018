```java

public class ClassLoadTest{
    public Class<?> loadClass(String name, boolean resolve){
        Class c = findLoadedClass(name);
        
        try{
            if(c == null){
                if(parent != null){
                    c = parent.loadClass(name, false);
                }else{
                    c = UseBootStrap(name);
                }
            }
        }catch(ClassNotFoundException e){
            
        }
        
        if(c == null){
            findClass(name); //本类加载器进行加载
        }
        
        if(resolve){
            resolveClass(c);
        }
        
        return c;
    }
}


```