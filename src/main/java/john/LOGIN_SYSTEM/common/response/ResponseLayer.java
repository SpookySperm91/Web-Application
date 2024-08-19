package john.LOGIN_SYSTEM.common.response;

import john.LOGIN_SYSTEM.persistenceMongodb.user.UserEntity;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.bson.types.ObjectId;
import org.springframework.http.HttpStatus;

import java.util.ArrayList;
import java.util.Date;

// RESPONSE BACK TO THE CONTROLLER FROM THE SERVICE
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ResponseLayer {
    private DataAccess dataAccess;
    private boolean success;
    private String message;
    private UserEntity userEntity;
    private HttpStatus httpStatus;
    private ResponseType type;

    public ResponseLayer(DataAccess dataAccess, Boolean success, String message, HttpStatus httpStatus) {
        this.dataAccess = dataAccess;
        this.success = success;
        this.message = message;
        this.httpStatus = httpStatus;
    }

    public ResponseLayer(DataAccess dataAccess, Boolean success, String message, ResponseType type, HttpStatus httpStatus) {
        this.dataAccess = dataAccess;
        this.success = success;
        this.message = message;
        this.type = type;
        this.httpStatus = httpStatus;
    }

    public ResponseLayer(Boolean success, String message, HttpStatus httpStatus, ResponseType type) {
        this.success = success;
        this.message = message;
        this.httpStatus = httpStatus;
        this.type = type;
    }

    public ResponseLayer(Boolean success, String message, ResponseType type, HttpStatus httpStatus) {
        this.success = success;
        this.message = message;
        this.type = type;
        this.httpStatus = httpStatus;
    }

    public ResponseLayer(DataAccess dataAccess, String message, ResponseType type, HttpStatus httpStatus) {
        this.dataAccess = dataAccess;
        this.message = message;
        this.httpStatus = httpStatus;
        this.type = type;
    }

    public ResponseLayer(String message, ResponseType type, HttpStatus httpStatus) {
        this.message = message;
        this.httpStatus = httpStatus;
        this.type = type;
    }

    public ResponseLayer(Boolean success, String message, HttpStatus httpStatus) {
        this.success = success;
        this.message = message;
        this.httpStatus = httpStatus;
    }

    public ResponseLayer(Boolean success, String message) {
        this.success = success;
        this.message = message;
    }

    public ResponseLayer(Boolean success, UserEntity userEntity) {
        this.success = success;
        this.userEntity = userEntity;
    }

    public ResponseLayer(Boolean success) {
        this.success = success;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class DataAccess {
        private ArrayList<String> strings = new ArrayList<>();
        private ArrayList<Integer> integers = new ArrayList<>();
        private ArrayList<Double> decimals = new ArrayList<>();
        private ArrayList<ObjectId> objectIds = new ArrayList<>();
        private ArrayList<Date> dates = new ArrayList<>();
        private ArrayList<Object> objects = new ArrayList<>();

        // Methods to add collections
        public void addStrings(ArrayList<String> strings) {
            this.strings.addAll(strings);
        }

        public void addIntegers(ArrayList<Integer> integers) {
            this.integers.addAll(integers);
        }

        public void addDecimals(ArrayList<Double> decimals) {
            this.decimals.addAll(decimals);
        }

        public void addObjectIds(ArrayList<ObjectId> objectIds) {
            this.objectIds.addAll(objectIds);
        }

        public void addDates(ArrayList<Date> dates) {
            this.dates.addAll(dates);
        }

        public void addObjects(ArrayList<Object> objects) {
            this.objects.addAll(objects);
        }

        // Overloaded methods to add single values
        public void addString(String string) {
            this.strings.add(string);
        }

        public void addInteger(Integer integer) {
            this.integers.add(integer);
        }

        public void addDecimal(Double decimal) {
            this.decimals.add(decimal);
        }

        public void addObjectId(ObjectId objectId) {
            this.objectIds.add(objectId);
        }

        public void addDate(Date date) {
            this.dates.add(date);
        }

        public void addObject(Object objects) {
            this.objects.add(objects);
        }
    }
}
