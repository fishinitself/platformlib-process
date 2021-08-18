package com.platformlib.process.ssh.provider;

import com.platformlib.process.provider.ProcessBuilderFactoryProvider;
import com.platformlib.process.ssh.SshConnection;
import com.platformlib.process.ssh.builder.SshProcessBuilder;
import com.platformlib.process.ssh.factory.SshProcessBuilderFactory;
import com.platformlib.process.ssh.specification.SshProcessSpec;

public class SshProcessBuilderFactoryProvider implements ProcessBuilderFactoryProvider<SshProcessBuilder> {
    @Override
    public boolean isSuitable(final Object specification) {
        return specification == null
                || specification instanceof SshProcessSpec
                || specification instanceof Class && SshProcessBuilder.class.isAssignableFrom((Class<?>) specification);
    }

    @Override
    public SshProcessBuilder newProcessBuilder(final Class<SshProcessBuilder> clazz) {
        return SshProcessBuilderFactory.newSshProcessBuilder();
    }

    @Override
    public SshProcessBuilder newProcessBuilder(final Object specification) {
        final SshProcessBuilder sshProcessBuilder = newProcessBuilder(SshProcessBuilder.class);
        if (specification instanceof SshConnection) {
            sshProcessBuilder.connectTo((SshConnection) specification);
        }
        /*
        //TODO Implement
        if (specification instanceof SshProcessSpec) {
        }
         */
        return sshProcessBuilder;
    }
}
