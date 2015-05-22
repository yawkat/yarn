yarn
====

Compile-time dependency injection and component framework inspired by spring and dagger.

Beans
-----

Beans are objects that can use dependency injection and in turn be injected into other objects. They can be created either using component scanning or through a method in another bean:

```
public class ProviderBean {
    [...]

    @Provides
    public OtherBean otherBean() {
        return new OtherBean();
    }
}
```

Components
----------

Components are beans that can be created from their constructor.

```
@Component
public class MyComponent {
    [...]
}
```

Dependency Injection
--------------------

Any bean can use dependency injection with the @javax.inject.Inject annotation. Injected fields, methods and constructors must never be private.

```
@Component
public class MyComponent {
    @Inject 
    MyComponent(Dependency d) {}

    @Inject Dependency d;

    @Inject
    void loadDependency(Dependency d) {}
}
```

Entry Points
------------

To actually get your components you need to define an entry point:

```
@EntryPoint
@ComponentScan
public interface Main {
    MyComponent myComponent();

    public static void main(String[] args) {
        Yarn.build(Main.class).myComponent().doStuff();
    }
}
```

You can include components with `@ComponentScan` and `@Include`. If either of those annotations is present, `@EntryPoint` is optional. The entry point can either be a class or an interface, abstract methods will be overridden with getters to the requested beans.
