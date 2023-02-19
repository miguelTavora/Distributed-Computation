package server;

import com.google.protobuf.Empty;
import contract.*;
import io.grpc.stub.StreamObserver;
import spreadcommon.StoreInformation;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

public class ServerUser extends QuestionsGrpc.QuestionsImplBase{

    private final String USER_ACCESS = "admin";
    private StoreInformation information;

    public ServerUser(StoreInformation information) {
        this.information = information;
    }

    @Override
    public void maxValueVelocity(Empty request, StreamObserver<VelocityComplete> responseObserver) {
        ArrayList<Integer> values = information.getValues();
        ArrayList<Long> sids = information.getIdentifiers();
        ArrayList<String> locals = information.getLocals();
        ArrayList<String> dates = information.getDates();

        // obatin the maximum value
        int maxValue = -1;
        int index = -1;
        for(int i = 0; i < values.size(); i++) {
            if(values.get(i) > maxValue) {
                index = i;
                maxValue = values.get(i);
            }
        }

        // sets the values if there is no value stored or the maximum value
        VelocityComplete.Builder velBuild = VelocityComplete.newBuilder();
        velBuild.setVelocity(maxValue);
        if(index != -1) {
            velBuild.setSid(sids.get(index).intValue());
            velBuild.setLocal(locals.get(index));
            velBuild.setData(dates.get(index));
        }
        else {
            velBuild.setSid(-1);
            velBuild.setLocal("");
            velBuild.setData("");
        }
        VelocityComplete vel = velBuild.build();

        responseObserver.onNext(vel);
        responseObserver.onCompleted();
    }

    @Override
    public void meanVelocity(Empty request, StreamObserver<Velocity> responseObserver) {
        ArrayList<Integer> values = information.getValues();
        int total = 0;
        for(int i = 0; i < values.size(); i++) {
            total += values.get(i);
        }
        if(values.size() > 0) total = (int) total/ values.size();

        Velocity vel  = Velocity.newBuilder().setVelocity(total).build();
        responseObserver.onNext(vel);
        responseObserver.onCompleted();
    }

    @Override
    public void meanVelocityBetweenDates(contract.Date request, StreamObserver<Velocity> responseObserver) {
        ArrayList<String> dates = information.getDates();
        ArrayList<Integer> values = information.getValues();
        String beginDate = request.getStartDate();
        String endDate = request.getEndDate();
        ArrayList<Integer> validIndexes = new ArrayList<Integer>();

        for(int i = 0; i < dates.size(); i++) {
            if(isDateBigger(beginDate, dates.get(i)) && isDateLower(endDate, dates.get(i))) {
                validIndexes.add((i));
            }
        }
        int sumValues = 0;
        for(int i = 0; i < validIndexes.size(); i++) {
            sumValues += values.get(validIndexes.get(i));
        }
        int result = 0;
        if(validIndexes.size() != 0)
            result = (int) sumValues/validIndexes.size();


        Velocity vel = Velocity.newBuilder().setVelocity(result).build();
        responseObserver.onNext(vel);
        responseObserver.onCompleted();
    }

    @Override
    public void numConsumers(Empty request, StreamObserver<Consumer> responseObserver) {
        int numConsumer = information.getConsumerNames().size();

        Consumer consumer = Consumer.newBuilder().setNumConsumers(numConsumer).build();
        responseObserver.onNext(consumer);
        responseObserver.onCompleted();
    }

    @Override
    public void executeConsumer(ConsumerName request, StreamObserver<Confirm> responseObserver) {
        String username = request.getConsumerName();

        Confirm.Builder conf = Confirm.newBuilder();
        if(username.equals(USER_ACCESS))
            conf.setConfirm(true).setText(information.getConsumerNames().toString());

        else
            conf.setConfirm(false).setText("Cannot create member because you don't have permission");

        Confirm confirm = conf.build();
        responseObserver.onNext(confirm);
        responseObserver.onCompleted();
    }

    private boolean isDateBigger(String beginDate, String receivedDate)  {
        SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy");
        Date date1 = null;
        Date date2 = null;
        try {
            date1 = sdf.parse(beginDate);
            date2 = sdf.parse(receivedDate);
        } catch (ParseException e) {
            System.out.println("Error parsing date");
            return false;
        }

        // before() will return true if and only if date1 is before date2
        if(date1.before(date2)){
            return true;
        }

        //equals() returns true if both the dates are equal
        if(date1.equals(date2))
            return true;

        return false;
    }

    private boolean isDateLower(String endDate, String receivedDate) {
        SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy");
        Date date1 = null;
        Date date2 = null;
        try {
            date1 = sdf.parse(endDate);
            date2 = sdf.parse(receivedDate);
        } catch (ParseException e) {
            System.out.println("Error parsing date");
            return false;
        }

        // before() will return true if and only if date1 is before date2
        if(date1.after(date2)){
            return true;
        }

        //equals() returns true if both the dates are equal
        if(date1.equals(date2))
            return true;

        return false;
    }


}
