package io.github.miniplaceholders.api;

import java.util.Collection;
import java.util.HashSet;
import java.util.Locale;
import java.util.Objects;
import java.util.Set;

import io.github.miniplaceholders.api.placeholder.AudiencePlaceholder;
import io.github.miniplaceholders.api.placeholder.RelationalPlaceholder;
import io.github.miniplaceholders.api.utils.Conditions;
import net.kyori.adventure.audience.ForwardingAudience;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.function.BiFunction;
import java.util.function.Predicate;

import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.text.minimessage.Context;
import net.kyori.adventure.text.minimessage.tag.Tag;
import net.kyori.adventure.text.minimessage.tag.resolver.ArgumentQueue;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;

import static java.util.Objects.requireNonNull;

final class ExpansionImpl implements Expansion {
    private final String name;
    private final Tags.Single[] audiencePlaceholders;
    private final Tags.Relational[] relationalPlaceholders;
    private final TagResolver globalPlaceholders;
    private final Class<? extends Audience> filterClass;
    private final Predicate<Audience> predicateFilter;

    ExpansionImpl(
        @NotNull final String expansionName,
        @Nullable final Collection<Tags.Single> audiencePlaceholders,
        @Nullable final Collection<Tags.Relational> relationalPlaceholders,
        @Nullable final TagResolver globalPlaceholders,
        @Nullable final Class<? extends Audience> filterClass,
        @Nullable final Predicate<Audience> predicateFilter){
            this.name = expansionName+"-";
            this.audiencePlaceholders = audiencePlaceholders != null
                ? audiencePlaceholders.toArray(Tags.Single[]::new)
                : new Tags.Single[0];
            this.relationalPlaceholders = relationalPlaceholders != null
                ? relationalPlaceholders.toArray(Tags.Relational[]::new)
                : new Tags.Relational[0];
            this.globalPlaceholders = globalPlaceholders == null
                ? TagResolver.empty()
                : globalPlaceholders;
            this.filterClass = filterClass;
            this.predicateFilter = predicateFilter;
    }

    @Override
    public @NotNull String name(){
        return this.name;
    }

    @Override
    public @NotNull TagResolver audiencePlaceholders(@NotNull Audience audience) {
        if (audiencePlaceholders.length == 0) return TagResolver.empty();

        requireNonNull(audience, "the audience cannot be null");

        if ((audience = expansionFilter(audience)) == null) return TagResolver.empty();

        final TagResolver.Builder placeholders = TagResolver.builder();
        for (final Tags.Single pl : this.audiencePlaceholders) {
            placeholders.resolver(pl.of(audience));
        }
        return placeholders.build();
    }

    private Audience expansionFilter(final Audience audience) {
        if (filterClass == null && predicateFilter == null) {
            return audience;
        }
        final boolean eligible = filterClass != null && filterClass.isInstance(audience)
                || predicateFilter != null && predicateFilter.test(audience);
        if (eligible) {
            return audience;
        }
        if (audience instanceof final ForwardingAudience.Single forward) {
            return expansionFilter(forward.audience());
        }
        return null;
    }

    @Override
    public @NotNull TagResolver relationalPlaceholders(@NotNull Audience audience, @NotNull Audience otherAudience){
        if (relationalPlaceholders.length == 0) return TagResolver.empty();

        requireNonNull(audience, "the audience cannot be null");
        requireNonNull(otherAudience, "the other audience cannot be null");

        audience = expansionFilter(audience);
        otherAudience = expansionFilter(otherAudience);
        if (audience == null || otherAudience == null) {
            return TagResolver.empty();
        }

        final TagResolver.Builder placeholders = TagResolver.builder();
        for (final Tags.Relational pl : this.relationalPlaceholders) {
            placeholders.resolver(pl.of(audience, otherAudience));
        }
        return placeholders.build();
    }

    @Override
    public @NotNull TagResolver globalPlaceholders(){
        return this.globalPlaceholders;
    }

    @Override
    public void register() {
        if (this.registered()) {
            throw new IllegalStateException("Expansion" + this.name + "is already registered");
        }
        MiniPlaceholders.expansions.add(this);
    }

    @Override
    public void unregister() {
        if (!this.registered()) {
            throw new IllegalStateException("Expansion " + this.name + " is not registered");
        }
        MiniPlaceholders.expansions.remove(this);
    }

    @Override
    public boolean registered() {
        return MiniPlaceholders.expansions.contains(this);
    }

    static final class Builder implements Expansion.Builder {
        private final String expansionName;
        private Set<Tags.Single> audiencePlaceholders;
        private Set<Tags.Relational> relationalPlaceholders;
        private TagResolver.Builder globalPlaceholders;
        private Class<? extends Audience> filterClass;
        private Predicate<Audience> predicateFilter;

        Builder(@NotNull final String name){
            this.expansionName = Conditions.nonNullOrEmptyString(name, () -> "expansion name")
                .toLowerCase(Locale.ROOT).concat("_");
        }

        @Override
        public @NotNull Builder audiencePlaceholder(@NotNull final String key, @NotNull final AudiencePlaceholder audiencePlaceholder){
            Conditions.nonNullOrEmptyString(key, () -> "placeholder key");
            requireNonNull(audiencePlaceholder, "the audience placeholder cannot be null");

            if (this.audiencePlaceholders == null) this.audiencePlaceholders = new HashSet<>(5);

            this.audiencePlaceholders.add(Tags.single(expansionName+key, audiencePlaceholder));
            return this;
        }

        @Override
        public @NotNull Builder relationalPlaceholder(@NotNull final String key, @NotNull final RelationalPlaceholder relationalPlaceholder){
            Conditions.nonNullOrEmptyString(key, () -> "placeholder key");
            requireNonNull(relationalPlaceholder, "the relational placeholder cannot be null");

            if (this.relationalPlaceholders == null) this.relationalPlaceholders = new HashSet<>(4);

            this.relationalPlaceholders.add(Tags.relational(expansionName+"rel_"+key, relationalPlaceholder));
            return this;
        }

        @Override
        public @NotNull Builder globalPlaceholder(@NotNull final String key, @NotNull final BiFunction<ArgumentQueue, Context, Tag> function){
            Conditions.nonNullOrEmptyString(key, () -> "placeholder key");
            requireNonNull(function, "the global placeholder cannot be null");

            if(this.globalPlaceholders == null) this.globalPlaceholders = TagResolver.builder();

            this.globalPlaceholders.tag(expansionName+key, function);
            return this;
        }

        @Override
        public @NotNull Builder globalPlaceholder(@NotNull final String key, @NotNull final Tag tag){
            Conditions.nonNullOrEmptyString(key, () -> "placeholder key");
            requireNonNull(tag, "the tag cannot be null");

            if(this.globalPlaceholders == null) this.globalPlaceholders = TagResolver.builder();

            this.globalPlaceholders.tag(expansionName+key, tag);
            return this;
        }

        @Override
        public @NotNull Builder filter(@Nullable final Class<? extends Audience> clazz) {
            this.filterClass = clazz;
            return this;
        }

        @Override
        public @NotNull Builder filter(@Nullable final Predicate<Audience> predicate){
            this.predicateFilter = predicate;
            return this;
        }

        @Override
        public @NotNull Expansion build(){
            return new ExpansionImpl(
                this.expansionName,
                this.audiencePlaceholders,
                this.relationalPlaceholders,
                this.globalPlaceholders != null ? this.globalPlaceholders.build() : null,
                this.filterClass,
                this.predicateFilter
            );
        }
    }

    @Override
    public boolean equals(Object o){
        if (this==o) return true;
        if (o == null || o.getClass() != ExpansionImpl.class) return false;
        return ((ExpansionImpl)o).name.equals(this.name);
    }

    @Override
    public int hashCode(){
        return Objects.hash(name.hashCode());
    }
}
