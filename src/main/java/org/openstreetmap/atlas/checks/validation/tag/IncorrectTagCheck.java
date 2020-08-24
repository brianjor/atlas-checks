package org.openstreetmap.atlas.checks.validation.tag;

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Predicate;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import org.openstreetmap.atlas.checks.base.BaseCheck;
import org.openstreetmap.atlas.checks.flag.CheckFlag;
import org.openstreetmap.atlas.geography.atlas.items.AtlasObject;
import org.openstreetmap.atlas.geography.atlas.items.Edge;
import org.openstreetmap.atlas.utilities.configuration.Configuration;


/**
 * This check flags {@link AtlasObject}s that have tags that do not follow the
 * typical format for tag values, lowercase alphanumeric with no spaces.
 *
 * @author v-brjor
 */
public class IncorrectTagCheck extends BaseCheck<Long>
{
    private static final long serialVersionUID = -7253350310979573132L;
    private static final String INCORRECT_TAG_INSTRUCTION_SINGULAR = "Concerns tag ";
    private static final String INCORRECT_TAG_INSTRUCTION_PLURAL = "Concerns tags ";
    private static final List<String> FALLBACK_INSTRUCTIONS = Arrays
            .asList(INCORRECT_TAG_INSTRUCTION_SINGULAR, INCORRECT_TAG_INSTRUCTION_PLURAL);
    /** A pattern that matches tag values that are in the typical format */
    private static final Pattern VALID_PATTERN = Pattern.compile("^[a-z0-9_]+( *; *[a-z0-9_]+)*$");
    private static final HashSet<String> DEFAULT_TAGS_TO_CHECK = new HashSet<>(Arrays.asList(
            "abutters", "access", "admin_level", "aerialway", "aeroway", "amenity",
            "barrier", "bicycle", "boat", "border_type", "boundary", "bridge", "building", "construction",
            "covered", "craft", "crossing", "cutting",
            "disused", "drive_in", "drive_through",
            "electrified", "embankment", "emergency",
            "fenced", "foot", "ford",
            "geological", "goods",
            "hgv", "highway", "historic",
            "internet_access",
            "landuse", "lanes", "leisure",
            "man_made", "military", "mooring", "motorboat", "mountain_pass", "natural", "noexit",
            "office",
            "power", "public_transport",
            "railway", "route",
            "sac_scale", "service", "shop", "smoothness", "sport", "surface",
            "tactile_paving", "toll", "tourism", "tracktype", "traffic_calming", "trail_visibility",
            "tunnel",
            "usage",
            "vehicle",
            "wall", "waterway", "wheelchair", "wood"
    ));
    private static final HashMap<String, Set<String>> DEFAULT_EXCEPTIONS = new HashMap<>(Map.of(
        "type", Set.of("associatedStreet", "turnlanes:lengths", "turnlanes:turns",
                "restriction:hgv", "restriction:caravan", "restriction:motorcar", "restriction:bus",
                "restriction:agricultural", "restriction:bicycle", "restriction:hazmat", "TMC"),
        "service", Set.of("drive-through"),
        "aerialway", Set.of("j-bar", "t-bar"),
        "surface", Set.of("concrete:plates", "concrete:lanes", "paving_stones:20", "paving_stones:30",
                "paving_stones:50", "cobblestone:10", "cobblestone:20", "cobblestone:flattened"),
        "shop", Set.of("e-cigarette"),
        "barrier", Set.of("full-height_turnstile"),
        "man_made", Set.of("MDF")
    ));

    /**
     * The default constructor that must be supplied. The Atlas Checks framework will generate the
     * checks with this constructor, supplying a configuration that can be used to adjust any
     * parameters that the check uses during operation.
     *
     * @param configuration
     *            the JSON configuration for this check
     */
    public IncorrectTagCheck(final Configuration configuration)
    {
        super(configuration);
    }

    /**
     * This function will validate if the supplied atlas object is valid for the check.
     *
     * @param object
     *            the atlas object supplied by the Atlas-Checks framework for evaluation
     * @return {@code true} if this object should be checked
     */
    @Override
    public boolean validCheckForObject(final AtlasObject object)
    {
        // any object with tags will be valid for this check
        // don't check edges that are part of a way that has a previous edge that was checked
        return !this.isFlagged(object.getOsmIdentifier());
    }

    /**
     * This is the actual function that will check to see whether the object needs to be flagged.
     *
     * @param object
     *            the atlas object supplied by the Atlas-Checks framework for evaluation
     * @return an optional {@link CheckFlag} object that
     */
    @Override
    protected Optional<CheckFlag> flag(final AtlasObject object)
    {
        if (object instanceof Edge) {
            this.markAsFlagged(object.getOsmIdentifier());
        }

        Map<String, String> tags = object.getOsmTags();
        if (tags.isEmpty())
        {
            return Optional.empty();
        }

        List<String> incorrectTags = tags.keySet()
                .stream()
                // Is this a tag we need to check the value of?
                .filter(DEFAULT_TAGS_TO_CHECK::contains)
                // Is the value of the tag not a valid tag value?
                .filter(Predicate.not(tag -> VALID_PATTERN.asMatchPredicate().test(tags.get(tag))))
                // Is the value not an exception?
                .filter(Predicate.not(tag ->
                {
                    if (DEFAULT_EXCEPTIONS.containsKey(tag))
                    {
                        return DEFAULT_EXCEPTIONS.get(tag).contains(tags.get(tag));
                    }
                    return false;
                }))
                .map(tag -> String.format("'%s=%s'", tag, tags.get(tag)))
                .collect(Collectors.toList());

        if (!incorrectTags.isEmpty())
        {
            String instruction;
            if (incorrectTags.size() == 1)
            {
                instruction = this.getLocalizedInstruction(0);
            } else {
                instruction = this.getLocalizedInstruction(1);
            }
            instruction = instruction + String.join(", ", incorrectTags);
            return Optional.of(createFlag(object, instruction));
        }
        return Optional.empty();
    }

    @Override
    protected List<String> getFallbackInstructions()
    {
        return FALLBACK_INSTRUCTIONS;
    }
}
