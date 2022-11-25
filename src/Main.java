/**
 * @author Miroslav Kovachev
 * ${DATE}
 * Methodia Inc.
 */

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

/**
 * @author Miroslav Kovachev
 * 24.11.2022
 */
public class Main {

    private static final String SEPARATOR = ",";

    public static void main(String[] args) {
        final Scanner scanner = new Scanner(System.in);
        final int numberOfRows = scanner.nextInt();
        scanner.nextLine();
        final ArrayList<Price> prices = new ArrayList<>();
        final ArrayList<Measurement> measurements = new ArrayList<>();
        for (int i = 0; i < numberOfRows; i++) {
            final String dataLine = scanner.nextLine();
            final char dataType = dataLine.charAt(0);
            switch (dataType) {
                case 'P':
                    prices.add(parsePrice(dataLine));
                    break;
                case 'Q':
                    measurements.add(parseMeasurement(dataLine));
                    break;
                default:
                    throw new RuntimeException("Invalid input");
            }
        }

        final ProportionalMeasurementDistributor proportionalMeasurementDistributor = new ProportionalMeasurementDistributor();
        final List<QuantityPricePeriod> qpps = proportionalMeasurementDistributor.distribute(prices,
                measurements);
        for (QuantityPricePeriod qpp : qpps) {
            System.out.println(qpp.toString());
        }

    }

    private static Measurement parseMeasurement(String dataLine) {
        final String[] components = dataLine.split(SEPARATOR);
        final String startDateStr = components[1];
        final String endDateStr = components[2];
        final String quantityStr = components[3];
        final ZonedDateTime startDate = ZonedDateTime.parse(startDateStr, DateTimeFormatter.ISO_ZONED_DATE_TIME);
        final ZonedDateTime endDate = ZonedDateTime.parse(endDateStr, DateTimeFormatter.ISO_ZONED_DATE_TIME);
        final BigDecimal quantity = new BigDecimal(quantityStr);

        return new Measurement(quantity, startDate, endDate);
    }

    private static Price parsePrice(String dataLine) {
        final String[] components = dataLine.split(SEPARATOR);
        final String startDateStr = components[1];
        final String endDateStr = components[2];
        final String priceStr = components[3];

        final LocalDate startDate = LocalDate.parse(startDateStr, DateTimeFormatter.ISO_DATE);
        final LocalDate endDate = LocalDate.parse(endDateStr, DateTimeFormatter.ISO_DATE);
        final BigDecimal price = new BigDecimal(priceStr);
        return new Price(startDate, endDate, price);
    }



    static class ProportionalMeasurementDistributor {

        private final static String ZONE_SOFIA = "Europe/Sofia";

        /**
         * Proportionally distributes the measurements based on the prices.
         * If there are two or more prices for a given measurement, it splits the period of the measurement into sub-periods.
         * Each sub-period is the intersection between the price period and the measurement period.
         * A quantity for the sub period is calculated
         * by dividing the days of the sub-period by the days of the whole period
         * and multiplying the resulting ratio by the whole quantity.
         * So the measurement's quantity is split proportionally by the days each sub-period takes.
         * The intermediate calculations have a three digit precision and the final result is rounded to two digits.
         * The last quantity is calculated by subtracting the amount for the previous periods from the whole amount.
         * This is done to prevent a rounding error.
         *
         * @param prices       ordered by start date
         * @param measurements ordered by start date
         * @return QPP ordered by start date
         */
        public List<QuantityPricePeriod> distribute(List<Price> prices, List<Measurement> measurements) {
            final ArrayList<QuantityPricePeriod> quantityPricePeriods = new ArrayList<>();
            for (Measurement measurement : measurements) {
                final List<Price> pricesForMeasurement = filterPricesByMeasurementIntersection(measurement, prices);
                ZonedDateTime lastDateTime = measurement.getStart();

                BigDecimal currentQuantitySum = BigDecimal.ZERO;
                for (Price price : pricesForMeasurement) {
                    final LocalDate priceEnd = price.getEnd();
                    final LocalDate measurementEnd = measurement.getEnd().toLocalDate();
                    final ZonedDateTime qppStart = lastDateTime;
                    final BigDecimal qppPrice = price.getPrice();
                    measurement.getStart().withMinute(0).withSecond(0).withNano(0);
                    final long measurementDays = daysInPeriodInclusive(measurement.getStart(), measurement.getEnd());

                    if (priceEnd.compareTo(measurementEnd) >= 0) {
                        final ZonedDateTime qppEnd = measurement.getEnd();
                        final BigDecimal qppQuantity = measurement.getQuantity().subtract(currentQuantitySum);
                        final QuantityPricePeriod quantityPricePeriod =
                                new QuantityPricePeriod(qppQuantity, qppStart, qppEnd, price);
                        quantityPricePeriods.add(quantityPricePeriod);
                    } else {
                        final ZonedDateTime qppEnd = price.getEnd().atTime(23, 59, 59)
                                .atZone(ZoneId.of(ZONE_SOFIA));

                        final long qppPeriodDays = daysInPeriodInclusive(lastDateTime, qppEnd);
                        final BigDecimal qppQuantity = BigDecimal.valueOf(qppPeriodDays)
                                .divide(BigDecimal.valueOf(measurementDays), 2, RoundingMode.HALF_UP)
                                .multiply(measurement.getQuantity());
                        final QuantityPricePeriod quantityPricePeriod =
                                new QuantityPricePeriod(qppQuantity, lastDateTime, qppEnd, price);
                        quantityPricePeriods.add(quantityPricePeriod);
                        lastDateTime = qppEnd.plusSeconds(1);
                        currentQuantitySum = currentQuantitySum.add(qppQuantity);
                    }

                }
            }

            return quantityPricePeriods;
        }

        private long daysInPeriodInclusive(ZonedDateTime start, ZonedDateTime end) {
            return start.until(end, ChronoUnit.DAYS) + 1;
        }

        private List<Price> filterPricesByMeasurementIntersection(Measurement measurement, List<Price> prices) {
            final ArrayList<Price> filteredPrices = new ArrayList<>();
            final ZonedDateTime measurementStart = measurement.getStart();
            final ZonedDateTime measurementEnd = measurement.getEnd();
            for (Price price : prices) {
                final ZonedDateTime priceStart = price.getStart().atTime(0, 0).atZone(ZoneId.of(ZONE_SOFIA));
                final ZonedDateTime priceEnd = price.getEnd().atTime(0, 0).plusDays(1).minusSeconds(1)
                        .atZone(ZoneId.of(ZONE_SOFIA));
                if (measurementStart.isBefore(priceEnd.plusDays(1)) && measurementEnd.isAfter(
                        priceStart.plusDays(1))) {
                    filteredPrices.add(price);
                }
            }
            return filteredPrices;
        }
    }

    static class Price {

        private LocalDate start;
        private LocalDate end;
        private BigDecimal price;

        public Price(final LocalDate start, final LocalDate end, final BigDecimal price) {
            this.start = start;
            this.end = end;
            this.price = price;
        }

        public LocalDate getStart() {
            return start;
        }

        public LocalDate getEnd() {
            return end;
        }

        public BigDecimal getPrice() {
            return price;
        }
    }

    static class Measurement {

        private BigDecimal quantity;
        private ZonedDateTime start;
        private ZonedDateTime end;

        public Measurement(final BigDecimal quantity, final ZonedDateTime start, final ZonedDateTime end) {
            this.quantity = quantity;
            this.start = start;
            this.end = end;
        }

        public BigDecimal getQuantity() {
            return quantity;
        }

        public ZonedDateTime getStart() {
            return start;
        }

        public ZonedDateTime getEnd() {
            return end;
        }
    }

    static class QuantityPricePeriod {

        private BigDecimal quantity;
        private ZonedDateTime start;
        private ZonedDateTime end;
        private Price price;

        public QuantityPricePeriod(final BigDecimal quantity, final ZonedDateTime start, final ZonedDateTime end,
                                   final Price price) {
            this.quantity = quantity;
            this.start = start;
            this.end = end;
            this.price = price;
        }

        public BigDecimal getQuantity() {
            return quantity;
        }

        public ZonedDateTime getStart() {
            return start;
        }

        public ZonedDateTime getEnd() {
            return end;
        }

        public Price getPrice() {
            return price;
        }

        @Override
        public String toString() {
            final DecimalFormat decimalFormat = new DecimalFormat("0.00");
            return String.format("%s,%s,%s,%s", DateTimeFormatter.ISO_OFFSET_DATE_TIME.format(start),
                    DateTimeFormatter.ISO_OFFSET_DATE_TIME.format(end), decimalFormat.format(quantity),
                    decimalFormat.format(price.getPrice()));
        }
    }
}
