package hellofx;

import org.graalvm.nativeimage.c.type.CCharPointer;
import org.graalvm.nativeimage.c.type.CTypeConversion;
import org.graalvm.word.ComparableWord;

import static hellofx.SampleCall.*;

public class MessageSendStruct implements CMessageSendStruct {
    private String producerName;
    private String topic;
    private String tags;
    private String keys;
    private String body;

    public MessageSendStruct(String producerName, String topic, String tags, String keys, String body) {
        this.producerName = producerName;
        this.topic = topic;
        this.tags = tags;
        this.keys = keys;
        this.body = body;
    }

    @Override
    public CCharPointer getProducerName() {
        return CTypeConversion.toCString(producerName).get();
    }

    @Override
    public void setProducerName(CCharPointer value) {
        this.producerName = CTypeConversion.toJavaString(value);

    }

    @Override
    public CCharPointer getTopic() {
        return CTypeConversion.toCString(topic).get();
    }

    @Override
    public void setTopic(CCharPointer value) {
        this.topic = CTypeConversion.toJavaString(value);

    }

    @Override
    public CCharPointer getTags() {
        return CTypeConversion.toCString(tags).get();

    }

    @Override
    public void setTags(CCharPointer value) {
        this.tags = CTypeConversion.toJavaString(value);

    }

    @Override
    public CCharPointer getKeys() {
        return CTypeConversion.toCString(keys).get();
    }

    @Override
    public void setKeys(CCharPointer value) {
        this.keys = CTypeConversion.toJavaString(value);
    }

    @Override
    public CCharPointer getBody() {
        return CTypeConversion.toCString(body).get();
    }

    @Override
    public void setBody(CCharPointer value) {
        this.body = CTypeConversion.toJavaString(value);
    }

    @Override
    public boolean isNull() {
        return false;
    }

    @Override
    public boolean isNonNull() {
        return false;
    }

    @Override
    public boolean equal(ComparableWord val) {
        return false;
    }

    @Override
    public boolean notEqual(ComparableWord val) {
        return false;
    }

    @Override
    public long rawValue() {
        return 0;
    }

    @Override
    public boolean equals(Object o) {
        return false;
    }
}
