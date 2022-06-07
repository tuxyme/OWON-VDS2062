#!/bin/bash

# copy edited source files to decompiled source in source/
cp -r edited_source/* source/

# compile
cd source && \
../jdk1.6.0_45/bin/javac -cp '../VDS_S2/plugins/*' com/owon/uppersoft/dso/wf/ChannelInfo.java && \
../jdk1.6.0_45/bin/javac -cp '../VDS_S2/plugins/*' com/owon/uppersoft/vds/util/SystemPropertiesUtil.java && \
../jdk1.6.0_45/bin/javac -cp '../VDS_S2/plugins/*' com/owon/uppersoft/vds/ui/prompt/KeepNoticeDialog.java && \
../jdk1.6.0_45/bin/javac -cp '../VDS_S2/plugins/*' com/owon/uppersoft/vds/ui/prompt/NoticeDialog.java && \
../jdk1.6.0_45/bin/javac -cp '../VDS_S2/plugins/*' com/owon/uppersoft/vds/socket/server/Server.java &&\
../jdk1.6.0_45/bin/javac -cp '../VDS_S2/plugins/*' com/owon/uppersoft/vds/socket/server/ServerControl.java &&\
../jdk1.6.0_45/bin/javac -cp '../VDS_S2/plugins/*' com/owon/uppersoft/dso/view/pane/dock/MarkValueBulletin.java &&\
cd .. && \

# copy generated class files to unpacked jar in build/
cp source/com/owon/uppersoft/dso/wf/*.class build/com/owon/uppersoft/dso/wf/  && \
cp source/com/owon/uppersoft/vds/util/*.class build/com/owon/uppersoft/vds/util/  && \
cp source/com/owon/uppersoft/vds/ui/prompt/*.class build/com/owon/uppersoft/vds/ui/prompt/  && \
cp source/com/owon/uppersoft/vds/socket/server/*.class build/com/owon/uppersoft/vds/socket/server/  && \
cp source/com/owon/uppersoft/dso/view/pane/dock/*.class build/com/owon/uppersoft/dso/view/pane/dock/  && \

# copy edited images (ch1 red --> green)
cp -r source/com/owon/uppersoft/dso/image/* build/com/owon/uppersoft/dso/image/ &&\

# replace jar in VDS_S2 tree
cd build && \
../jdk1.6.0_45/bin/jar cf ../VDS_S2/plugins/com.owon.vds.foundation_1.0.0.jar .  && \
cd .. &&\
# copy a launch script in the VDS_S2 directory
cp edited_source/launch VDS_S2/ &&\
echo "To start the application enter the VDS_S2 directory and run the launch script"
