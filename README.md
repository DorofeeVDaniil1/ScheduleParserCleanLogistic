﻿# ScheduleParserCleanLogistic

## Описание
ScheduleParserCleanLogistic — это приложение для помощи в устновке графиков КГ, приложение недобавляет графики КГ. 
Приложение создает листы с ошибочными графиками КГ и верными. Для верных составляет SQL запрос для импорта в БД, который нужно будет запустить отдельно:
### Данные: 
В этот лист нужно вставить данные указанные в 1 строке, а именно:
  - КОД КП (lk_code)
  - Количество КГ (amount)
  - Объем КГ (volume)
  - Долгота (latitude)
  - Широта (longitude)
  - Расписание Графика КГ (schedule)
  - Адресс КП (address)
![](/images/Data.png)
### Вы также можете не указывать все поля, минимальные данные это:
- КОД КП (lk_code)
- ГРАФИК (schedule)



### Ошибки в расписании:
На этом листе записан Код КП и название графика, который не подходит под формат.
![](/images/error_in_schedule.png)
### Корректные данные:
На этом листе создаются те гриафики, которые можно импортировать. Для удобства приложение сразу создает SQL запрос для обновления в столбце C. Столбец A - Код КП, Столбец B- id грфаика в базе. 
![](/images/correct_data.png)
## Быстрый старт
1. Введите домен сервера, например: `amur.mytko.ru`.
2. Укажите логин и пароль для входа в систему.
3. **Введите ID графика "По заявке"** — этот ID используется для отсеивания графиков, которые не подходят под шаблон.
4. Укажите путь для сохранения файла Excel (укажите папку, куда должен быть сохранён файл).
5. Заполните Excel-файл, вставив свои данные в первый столбец.

### Как получить ID графика "По заявке"?
  1. Запустите приложение и добавьте график "По заявке" в столбец `schedule`.
  2. После завершения работы программы, откройте лист **"Корректные данные"** и найдите ID графика в столбце **B**.
  3. Перезапустите приложение с этими данными.
  ![](/images/get_id_1.png)
   - Также можно запустить программу с неподходящими графиками — ID графика отобразится в консоли между `%ID графика%`.
     ![](/images/get_id_2.png)

## Требования
- Доступ к серверу с указанным доменом.
- Правильные данные для авторизации (логин и пароль).
- Excel-файл для загрузки данных.
- Java SE 11

## Примечания
- Убедитесь, что ID графика "По заявке" правильно идентифицирован, чтобы программа корректно отбрасывала неподходящие графики.
- Для работы с приложением требуется установленная программа Excel.

## Лицензия
Этот проект распространяется на условиях лицензии MIT.
