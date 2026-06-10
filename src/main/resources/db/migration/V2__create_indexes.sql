
CREATE INDEX IF NOT EXISTS idx_movie_title ON public.movie (title);
CREATE INDEX IF NOT EXISTS idx_movie_director ON public.movie (director);
CREATE INDEX IF NOT EXISTS idx_movie_release_year ON public.movie (release_year);
CREATE INDEX IF NOT EXISTS idx_movie_avg_rating ON public.movie (avg_rating);

CREATE INDEX IF NOT EXISTS idx_movie_genre_movie_id ON public.movie_genre (movie_id);
CREATE INDEX IF NOT EXISTS idx_movie_genre_genre ON public.movie_genre (genre);

CREATE INDEX IF NOT EXISTS idx_movie_image_movie_id ON public.movie_image (movie_id);
CREATE INDEX IF NOT EXISTS idx_movie_image_type ON public.movie_image (type);

CREATE INDEX IF NOT EXISTS idx_movie_review_movie_id ON public.movie_review (movie_id);
CREATE INDEX IF NOT EXISTS idx_movie_review_username ON public.movie_review (username);
CREATE INDEX IF NOT EXISTS idx_movie_review_rating ON public.movie_review (rating);