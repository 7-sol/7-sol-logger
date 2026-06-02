package com._7_sol.logger.config;

import com._7_sol.logger.util.AuditLog;
import com._7_sol.logger.util.LogEntry;
import com._7_sol.logger.util.ObjectIdDeserializer;
import org.springframework.aot.hint.MemberCategory;
import org.springframework.aot.hint.RuntimeHints;
import org.springframework.aot.hint.RuntimeHintsRegistrar;
import org.springframework.lang.Nullable;

import java.util.Arrays;
import java.util.List;

public class LoggerHint  implements RuntimeHintsRegistrar {

    @Override
    public void registerHints(RuntimeHints hints, @Nullable ClassLoader classLoader) {

        // 1. Define the categories Jackson needs (Constructors are mandatory!)
        MemberCategory[] jacksonCategories = {
                MemberCategory.INVOKE_DECLARED_CONSTRUCTORS, // CRITICAL FOR JACKSON
                MemberCategory.INVOKE_DECLARED_METHODS,
                MemberCategory.INVOKE_PUBLIC_METHODS,
        };

        // 2. Just put the Class objects directly into the list
        List<Class<?>> classes = Arrays.asList(
                AuditLog.class,
                LogEntry.class,
                ObjectIdDeserializer.class
        );

        // 3. Loop through and register them cleanly
        for (Class<?> clazz : classes) {
            hints.reflection().registerType(clazz, jacksonCategories);
        }
    }
}
