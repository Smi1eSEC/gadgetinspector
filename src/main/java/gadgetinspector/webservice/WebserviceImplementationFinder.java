package gadgetinspector.webservice;

import gadgetinspector.ImplementationFinder;
import gadgetinspector.SerializableDecider;
import gadgetinspector.data.MethodReference;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class WebserviceImplementationFinder implements ImplementationFinder {
    private final WebserviceSerializableDecider webserviceSerializableDecider;
    private final Map<MethodReference.Handle, Set<MethodReference.Handle>> methodImplMap;
    public WebserviceImplementationFinder(WebserviceSerializableDecider webserviceSerializableDecider, Map<MethodReference.Handle, Set<MethodReference.Handle>> methodImplMap){
        this.webserviceSerializableDecider = webserviceSerializableDecider;
        this.methodImplMap = methodImplMap;
    }
    /*
    找调用链时获取某个类的所有实现类
     */
    @Override
    public Set<MethodReference.Handle> getImplementations(MethodReference.Handle target) {
        Set<MethodReference.Handle> allImpls = new HashSet<>();

        // For jackson search, we don't get to specify the class; it uses reflection to instantiate the
        // class itself. So just add the target method if the target class is serializable.
        if (Boolean.TRUE.equals(webserviceSerializableDecider.apply(target.getClassReference()))) {
            allImpls.add(target);
        }
        // 当为java.lang.Object 或其他常用内置类时会返回大量方法，因此忽略掉
        if (target.getClassReference().getName().equals("java/lang/Object") || target.getClassReference().getName().startsWith("java/") || target.getClassReference().getName().startsWith("javax/") || target.getClassReference().getName().startsWith("com/sun/") || target.getClassReference().getName().startsWith("sun/")) {
            return allImpls;
        }
        Set<MethodReference.Handle> subClassImpls = methodImplMap.get(target);
        if (subClassImpls != null) {
            for (MethodReference.Handle subClassImpl : subClassImpls) {
                if (Boolean.TRUE.equals(webserviceSerializableDecider.apply(subClassImpl.getClassReference())) && target.getName().equals(subClassImpl.getName())) {
                    allImpls.add(subClassImpl);
                }
            }
        }

        return allImpls;
    }
}
