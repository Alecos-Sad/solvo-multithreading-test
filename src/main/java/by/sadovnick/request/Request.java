package by.sadovnick.request;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public class Request {
    private final int type; //тип запроса (А или В)
    private final int value; //признак Х
}
