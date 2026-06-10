CREATE SEQUENCE IF NOT EXISTS movie_id_seq;
CREATE SEQUENCE IF NOT EXISTS movie_image_id_seq;
CREATE SEQUENCE IF NOT EXISTS movie_review_review_id_seq;

CREATE TABLE IF NOT EXISTS public.movie
(
    movie_id    bigint NOT NULL DEFAULT nextval('movie_id_seq'),
    title       character varying(255) NOT NULL,
    overview    character varying(255),
    release_year integer NOT NULL,
    director    character varying(255) NOT NULL,
    avg_rating  double precision DEFAULT 0.0,
    rating_count integer DEFAULT 0,
    created_at  timestamp without time zone DEFAULT now(),
    updated_at  timestamp without time zone DEFAULT now(),
    CONSTRAINT movie_pkey PRIMARY KEY (movie_id),
    CONSTRAINT movie_release_year_check CHECK (release_year >= 1888 AND release_year <= 2100)
);

CREATE TABLE IF NOT EXISTS public.movie_genre
(
    movie_id bigint NOT NULL,
    genre    character varying(255) NOT NULL,
    CONSTRAINT moviegenres_pkey PRIMARY KEY (movie_id, genre),
    CONSTRAINT moviegenres_movie_id_fkey FOREIGN KEY (movie_id)
    REFERENCES public.movie (movie_id)
    ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS public.movie_image
(
    image_id   bigint NOT NULL DEFAULT nextval('movie_image_id_seq'),
    movie_id   bigint NOT NULL,
    url        character varying(255) NOT NULL,
    type       character varying(255) NOT NULL,
    created_at timestamp without time zone DEFAULT now(),
    CONSTRAINT movie_image_pkey PRIMARY KEY (image_id),
    CONSTRAINT movie_image_movie_id_fkey FOREIGN KEY (movie_id)
    REFERENCES public.movie (movie_id) ON DELETE CASCADE,
    CONSTRAINT movie_image_type_check CHECK (type IN ('COVER', 'SLIDE'))
);

CREATE TABLE IF NOT EXISTS public.movie_review
(
    review_id  bigint NOT NULL DEFAULT nextval('movie_review_review_id_seq'),
    movie_id   bigint NOT NULL,
    username   character varying(255) NOT NULL,
    rating     integer NOT NULL,
    comment    character varying(1000),
    created_at timestamp without time zone DEFAULT now(),
    CONSTRAINT movie_review_pkey PRIMARY KEY (review_id),
    CONSTRAINT unique_user_movie_review UNIQUE (movie_id, username),
    CONSTRAINT movie_review_movie_id_fkey FOREIGN KEY (movie_id)
    REFERENCES public.movie (movie_id) ON DELETE CASCADE,
    CONSTRAINT movie_review_rating_check CHECK (rating >= 1 AND rating <= 10)
);