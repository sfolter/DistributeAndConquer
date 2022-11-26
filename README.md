# DistributeAndConquer

> **Съставител: Мирослав Ковачев**, [Methodia](https://methodia.com)
>
>**Методия** е технологичен доставчик на персонализирани бизнес решения от типа „бизнес като услуга“ (BaaS) и „софтуер като услуга“ (SaaS), насочени към енергийната и ютилити индустрии. Компанията е фокусирана върху разработката на дигитални продукти и на цялостен софтуер за управление на комплексни оперативни дейности на доставчици на електричество, газ, вода, отопление и други комунални услуги.
>
>**Първият участник, който се справи със задачата, ще получи специална награда от Методия!**

## Предистория
Петър иска да стане инкасатор и да направи сметките за ток по-коректни за всички.
Инкасаторът е длъжен да **събира показанията на електромерите** и да **изчислява задължението за всеки абонат на база цената**. Проблемът е, че цената на тока в този регион се мени често и това създава усложнения, защото **Петър не може да събира показанията със същата честота** и това налага да има **разпределение на използваната електроенергия.** Петър ще ви помогне с изискванията, а вие ще напишете програма, за да го улесните!

## Детайли
Когато за отчетената консумирана електроенергия за период има повече от една цена, Петър иска да **дистрибутира пропорционално количеството** на база на подпериодите, на които цените разделят общия период на консумираната електроенергия за даден отчетен период.
При разпределението на количеството може да се натрупа **грешка от закръгляне**, затова Петър иска последният подпериод да бъде изчислен като разлика на общата електроенергия и сумата от разпределената електроенергия (без последния период).
Тъй като цената може да се мени **най-много веднъж в рамките на деня**, ще трябва да делим периода по дни, на започнат ден.

### Пример
За отчетен период `01-11-2022Т13:23:00+02:00` до `30-11-2022T15:20:00+02:00` са използвани **120.00kWh** електроенергия.
Цените, които обхващат периода са:  
`25-10-2022` - `06-11-2022` - 0.30 лв/кВч  
`07-11-2022` - `18-11-2022` - 0.35 лв/кВч  
`19-11-2022` - `04-12-2022` - 0.32 лв/кВч

Смените на цената определят подпериодите. Брой подпериоди = брой различни цени
Тъй като е пропорционално по дните, трябва да определим колко дни обхваща всеки подпериод:  
Подпериод 1: `01-11-2022Т13:23:00+02:00` - `06-11-2022Т23:59:59+02:00` - **6 дена**  
Подпериод 2: `07-11-2022Т00:00:00+02:00` - `18-11-2022Т23:59:59+02:00` - **12 дена**  
Подпериод 3: `19-11-2022Т00:00:00+02:00` - `30-11-2022T15:20:00+02:00` - **12 дена**

Общо за отчетния период дните са **30**.  
**Трябва да разпределим:**  
Подпериод 1: `120*(6/30) = 24 kWh`  
Подпериод 2: `120*(12/30) = 48 kWh`  
Подпериод 3: `120 - (Подпериод1 + Подпериод2) = 120 - 72 = 48 kWh`  

# Вход
Входът ще бъде от множество редове в **CSV** формат, които описват период и потребено количество или период и цена.
**На първи ред ще получите броя на оставащите редове**, които трябва да изчетете от стандартния вход.  
Форматът на ред за период и цена е:
```text_pseudo
P,{НАЧАЛНА_ДАТА},{КРАЙНА_ДАТА},{ЦЕНА}
```
`{НАЧАЛНА_ДАТА}` e дата във формат yyyy-MM-dd  
`{КРАЙНА_ДАТА}` e дата във формат yyyy-MM-dd  
`{ЦЕНА}` е цената за kWh в десетичен формат. **Подава се без мерна единица.**

Форматът на ред за количество е:
``` text_pseudo
Q,{НАЧАЛНА_ДАТА_ЧАС},{КРАЙНА_ДАТА_ЧАС},{КОЛИЧЕСТВО}
```
`{НАЧАЛНА_ДАТА_ЧАС}` е дата, час и зона във формат `yyyy-MM-dd'T'HH:mm:ssz`  
`{КРАЙНА_ДАТА_ЧАС}` e дата, час и зона във формат `yyyy-MM-dd'T'HH:mm:ssz`  
`{КОЛИЧЕСТВО}` e потребената електроенергия в kW/h, в десетичен формат

Редовете за цени се подават в нарастващ ред по `{НАЧАЛНА_ДАТА}`
Редовете за количества се подават в нарастващ ред по `{НАЧАЛНА_ДАТА_ЧАС}`
**Между двата типа данни няма специфична поредност.**

# Ограничения
Цена:

`{НАЧАЛНА_ДАТА}` е по стандарт **ISO8601**/[**RFC3339**](https://www.rfc-editor.org/rfc/rfc3339#ref-ISO8601).  
`{КРАЙНА_ДАТА}` е по стандарт **ISO8601**/[**RFC3339**](https://www.rfc-editor.org/rfc/rfc3339#ref-ISO8601).

Валидността на цените е включителна с точност до секунда. Подразбира се, че цената е валидна от **00:00:00** часа на `{НАЧАЛНА_ДАТА}` до **23:59:59** на

`{КРАЙНА_ДАТА}`. Валидността на цените е за часова зона **Europe/Sofia**.  
`{ЦЕНА}` e положително реално число. `0 < {ЦЕНА} <= 1000`

Цената може да се мени не повече от един път на ден.  
Използвана електроенергия:  
`{НАЧАЛНА_ДАТА_ЧАС}` е по стандарт **ISO8601**/[**RFC3339**](https://www.rfc-editor.org/rfc/rfc3339#ref-ISO8601).   
`{КРАЙНА_ДАТА_ЧАС}` е по стандарт **ISO8601**/[**RFC3339**](https://www.rfc-editor.org/rfc/rfc3339#ref-ISO8601).

Периодът на използваната електроенергия е с точност до секунда.  
`{КОЛИЧЕСТВО}` e положително реално число. `0 < {КОЛИЧЕСТВО} <= 1000000`

**Всички изчисления се правят с точност до 2-ри знак, закръгля се нагоре.**

# Изход
``` text_pseudo
{НАЧАЛНА_ДАТА_ЧАС},{КРАЙНА_ДАТА_ЧАС},{КОЛИЧЕСТВО},{ЦЕНА}
```
Изходът е **CSV**. Трябва да е подреден по `{НАЧАЛНА_ДАТА_ЧАС}` в нарастващ ред.  
`{НАЧАЛНА_ДАТА_ЧАС},{КРАЙНА_ДАТА_ЧАС}` трябва да са във формат `yyyy-MM-dd'T'HH:mm:ssz`. Задължително зоната трябва да е текущата за `[Europe/Sofia]`. Зоната не трябва да включва името на региона: `2022-11-26T09:00:00+02:00[Europe/Sofia]` се счита за невалиден изход. `2022-11-26T09:00:00+02:00` е валиден.  
`{КОЛИЧЕСТВО}` трябва да е във формат на десетично число с точност два знака след запетаята.  
`{ЦЕНА}` трябва да е във формат на десетично число с точност два знака след запетаята.

**Не трябва** да се включва друго, като например мерна единица.
Да се включват и незначителните нули с точносто два знака след точката. Например:
`3` е невалиден изход, `3.00` e валиден.




Жокер 1:
За отчетен период `01-11-2022Т13:23:00+02:00` до `30-11-2022T15:20:00+02:00` са използвани **120.00kWh** електроенергия.
Цените, които обхващат периода са:  
`25-10-2022` - `06-11-2022` - 0.30 лв/кВч  
`07-11-2022` - `18-11-2022` - 0.35 лв/кВч  
`19-11-2022` - `04-12-2022` - 0.32 лв/кВч

Смените на цената определят подпериодите. Брой подпериоди = брой различни цени
Тъй като е пропорционално по дните, трябва да определим колко дни обхваща всеки подпериод:  
Подпериод 1: `01-11-2022Т13:23:00+02:00` - `06-11-2022Т23:59:59+02:00` - **6 дена**  
Подпериод 2: `07-11-2022Т00:00:00+02:00` - `18-11-2022Т23:59:59+02:00` - **12 дена**  
Подпериод 3: `19-11-2022Т00:00:00+02:00` - `30-11-2022T15:20:00+02:00` - **12 дена**

Общо за отчетния период дните са **30**.  
**Трябва да разпределим:**  
Подпериод 1: `120*(6/30) = 24 kWh`  
Подпериод 2: `120*(12/30) = 48 kWh`  
Подпериод 3: `120 - (Подпериод1 + Подпериод2) = 120 - 72 = 48 kWh`

Жокер 2:
Тук има два отчетни периода. Първият отчетен период има само една цена. Вторият отчетен период има 3 цени и застъпва последния ден на първата цена.

Определяме подпериодите и дните, които обхваща всеки:  
Отчетен период 1 обхваща общо **6 дена**.  
Отчет 1 - Подпериод 1: `2022-11-01T13:23:00+02:00` - `2022-11-06T15:20:00+02:00` - **6 дена**  
Отчетен период 2 обхваща **25 дена**.  
Отчет 2 - Подпериод 1: `2022-11-06T15:20:01+02:00` - `2022-11-06T23:59:59+02:00` - **1 ден**  
Отчет 2 - Подпериод 2: `2022-11-07T00:00:00+02:00` - `2022-11-18T23:59:59+02:00` - **12 дена**  
Отчет 2 - Подпериод 3: `2022-11-19T00:00:00+02:00` - `2022-11-30T20:20:00+02:00` - **12 дена**

**Разпределение:**  
За първия отчетен период няма нужда от разпределяне, цялото количество 20кВч е на една цена - 0.30 лв/кВч.

За втори отчетен период разпределението е:
Подпериод 1: `100*(1/25) = 4 kWh`  
Подпериод 2: `100*(12/25) = 48 kWh`  
Подпериод 3: `100 - (Подпериод1 + Подпериод2) = 100 - 52 = 48 kWh`

Жокер 3:
**Разпределение**  
Отчетен период 1:  
**Общо 19 дена**  
Подпериод 1 (6 дена): `19.23 * (6/19) = 19.23 * 0.32 = 6.15 kWh`  
Подпериод 2 (11 дена): `19.23 * (11/19) = 19.23 * 0.58 = 11.15 kWh`  
Подпериод 3 (2 дена): `19.23 - (Подпериод1 + Подпериод2) = 19.23 - 17.30 = 1.93 kWh`  
Отчетен период 2:  
В този период има смяна на часовата зона.  
**Общо 14 дена**  
Подпериод 1 (12 дена): `37.81 * (12/14) = 37.81 * 0.86 = 32.52 kWh`  
Подпериод 2 (2 дена): `37.81 - Подпериод1 = 37.81 - 32.52 = 5.29 kWh`  
Отчетен период 3:  
**Общо 14 дена**  
Подпериод 1 (7 дена): `42.42 * (7/14) = 42.42 * 0.50 = 21.21 kWh`  
Подпериод 2 (7 дена): `42.42 - 21.21 = 21.21 kWh`  
