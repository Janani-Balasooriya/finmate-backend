package com.finmate.finmate_backend.income.sources.dto;

public class IncomeSourceDtos {

        public record IncomeSourceItem(
                        Long id,
                        String name,
                        String color) {
        }

        public record CreateIncomeSourceRequest(
                        String name,
                        String color) {
        }

        public record UpdateIncomeSourceRequest(
                        String name,
                        String color,
                        Boolean active) {
        }

}
