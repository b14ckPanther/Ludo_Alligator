package Model;

import java.util.Collection;
import java.util.Collections;
import java.util.EnumMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * The board: the shared track, the home columns, and the route of every
 * color from its start square to its finish. It is made by {@link BoardFactory}
 * and doesn't change during the game.
 *
 * <p>A piece's position is just an index in its color's route, so moving
 * a piece is adding the dice value to that index.
 */
public final class Board {
    private final int size;
    private final Map<Integer, Square> cells = new LinkedHashMap<>();
    private final Map<PlayerColor, List<Square>> routes = new EnumMap<>(PlayerColor.class);
    private List<Square> track = List.of();

    Board(int size) {
        this.size = size;
    }

    void put(Square square) {
        cells.put(square.row() * size + square.col(), square);
    }

    void setTrack(List<Square> track) {
        this.track = Collections.unmodifiableList(track);
    }

    void setRoute(PlayerColor color, List<Square> route) {
        routes.put(color, Collections.unmodifiableList(route));
    }

    /** Number of rows (and columns) of the grid. */
    public int size() {
        return size;
    }

    /** The square at this grid cell, or null (castles, center and empty cells). */
    public Square at(int row, int col) {
        if (row < 0 || col < 0 || row >= size || col >= size) {
            return null;
        }
        return cells.get(row * size + col);
    }

    /** All squares except the finish squares. */
    public Collection<Square> squares() {
        return Collections.unmodifiableCollection(cells.values());
    }

    /** The shared track in clockwise order. */
    public List<Square> track() {
        return track;
    }

    /** The whole route of a color: index 0 is its start square, the last index is its finish. */
    public List<Square> route(PlayerColor color) {
        return routes.get(color);
    }

    public Square finish(PlayerColor color) {
        List<Square> route = routes.get(color);
        return route.get(route.size() - 1);
    }

    /** Index of the finish square in a route (the same for every color). */
    public int finishIndex() {
        return routes.get(PlayerColor.RED).size() - 1;
    }

    /** First index of the home column in a route. */
    public int homeColumnStart() {
        return finishIndex() - BoardFactory.HOME_SQUARES;
    }

    public long count(SquareType type) {
        return cells.values().stream().filter(s -> s.type() == type).count();
    }
}
