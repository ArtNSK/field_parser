# field_parser
На вход в класс передается объект, поля которого требуется распарсить.
Обход полей объекта реализован методом прямого обхода. 

Методом getFields возвращается разбиение переданного объекта на составные
поля примитивов и поля объектов базовых классов (из пакета java.*).

Метод getFieldNames возвращает сокращенные названия данных полей.

Если переданный объект не имеет массивов, коллекций, а также полей
с null-значением, то класс методом getBaseObjectList, возвращает
значения полей объекта, методом getStringValueFromObjects
возвращает их строковое представление.
