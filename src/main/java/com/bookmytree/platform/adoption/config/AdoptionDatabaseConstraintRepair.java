package com.bookmytree.platform.adoption.config;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class AdoptionDatabaseConstraintRepair {

    private final JdbcTemplate jdbcTemplate;

    @PostConstruct
    public void repairConstraint() {
        jdbcTemplate.execute("""
            DO $$
            BEGIN
                IF to_regclass('public.adoption_listings') IS NOT NULL THEN
                    IF EXISTS (
                        SELECT 1
                        FROM pg_constraint
                        WHERE conrelid = 'public.adoption_listings'::regclass
                          AND conname = 'adoption_listings_category_check'
                    ) THEN
                        ALTER TABLE public.adoption_listings DROP CONSTRAINT adoption_listings_category_check;
                    END IF;

                    ALTER TABLE public.adoption_listings
                        ADD CONSTRAINT adoption_listings_category_check
                        CHECK (((category)::text = ANY (
                            ARRAY[
                                'CROP_FIELD'::character varying,
                                'FISH_POND'::character varying,
                                'FRUIT_FARM'::character varying,
                                'TIMBER_PLANT'::character varying,
                                'SINGLE_PLANT'::character varying,
                                'TREE'::character varying,
                                'PLANT'::character varying
                            ]
                        )));
                END IF;
            END $$;
            """);
    }
}
