package at.freathedge.games.connected.util.map;

import org.newdawn.slick.*;
import org.newdawn.slick.tiled.TileSet;
import org.newdawn.slick.tiled.TiledMap;
import org.newdawn.slick.util.Log;
import org.newdawn.slick.util.ResourceLoader;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

public class BetterTileSet {
    private final BetterTiledMap map;
    public int index;
    public String name;
    public int firstGID;
    public int lastGID = Integer.MAX_VALUE;
    public int tileWidth;
    public int tileHeight;
    public SpriteSheet tiles;
    public int tilesAcross;
    public int tilesDown;
    private HashMap props = new HashMap();
    protected int tileSpacing = 0;
    protected int tileMargin = 0;
    private final Map<Integer, Animation> animatedTiles = new HashMap<>();


    public BetterTileSet(BetterTiledMap map, Element element, boolean loadImage) throws SlickException {
        this.map = map;
        this.name = element.getAttribute("name");
        this.firstGID = Integer.parseInt(element.getAttribute("firstgid"));
        String source = element.getAttribute("source");
        if (source != null && !source.equals("")) {
            try {
                InputStream in = ResourceLoader.getResourceAsStream(map.getTilesLocation() + "/" + source);
                DocumentBuilder builder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
                Document doc = builder.parse(in);
                Element docElement = doc.getDocumentElement();
                element = docElement;
            } catch (Exception e) {
                Log.error(e);
                throw new SlickException("Unable to load or parse sourced tileset: " + this.map.tilesLocation + "/" + source);
            }
        }

        String tileWidthString = element.getAttribute("tilewidth");
        String tileHeightString = element.getAttribute("tileheight");
        if (tileWidthString.length() != 0 && tileHeightString.length() != 0) {
            this.tileWidth = Integer.parseInt(tileWidthString);
            this.tileHeight = Integer.parseInt(tileHeightString);
            String sv = element.getAttribute("spacing");
            if (sv != null && !sv.equals("")) {
                this.tileSpacing = Integer.parseInt(sv);
            }

            String mv = element.getAttribute("margin");
            if (mv != null && !mv.equals("")) {
                this.tileMargin = Integer.parseInt(mv);
            }

            NodeList list = element.getElementsByTagName("image");
            Element imageNode = (Element)list.item(0);
            String ref = imageNode.getAttribute("source");
            Color trans = null;
            String t = imageNode.getAttribute("trans");
            if (t != null && t.length() > 0) {
                int c = Integer.parseInt(t, 16);
                trans = new Color(c);
            }

            if (loadImage) {
                Image image = new Image(map.getTilesLocation() + "/" + ref, false, 2, trans);
                this.setTileSetImage(image);
            }

            NodeList pElements = element.getElementsByTagName("tile");

            for (int i = 0; i < pElements.getLength(); ++i) {
                Element tileElement = (Element) pElements.item(i);
                int id = Integer.parseInt(tileElement.getAttribute("id"));
                id += this.firstGID;
                Properties tileProps = new Properties();

                // Check if the tile has an animation
                NodeList animationNodes = tileElement.getElementsByTagName("animation");
                if (animationNodes.getLength() > 0) {
                    Animation animation = new Animation();
                    NodeList frameList = ((Element) animationNodes.item(0)).getElementsByTagName("frame");

                    for (int j = 0; j < frameList.getLength(); j++) {
                        Element frameElement = (Element) frameList.item(j);
                        int frameTileId = Integer.parseInt(frameElement.getAttribute("tileid"));
                        int duration = Integer.parseInt(frameElement.getAttribute("duration"));

                        int tilesetWidth = tiles.getHorizontalCount();
                        Image frameImage = tiles.getSprite(frameTileId % tilesetWidth, frameTileId / tilesetWidth);
                        animation.addFrame(frameImage, duration);
                    }

                    animation.setAutoUpdate(true);
                    animatedTiles.put(id, animation);

                    continue; // WICHTIG: nicht mehr properties verarbeiten wenn Animation
                }

                // Properties laden (wenn KEINE Animation)
                Element propsElement = (Element) tileElement.getElementsByTagName("properties").item(0);
                if (propsElement != null) {
                    NodeList properties = propsElement.getElementsByTagName("property");
                    for (int p = 0; p < properties.getLength(); ++p) {
                        Element propElement = (Element) properties.item(p);
                        String name = propElement.getAttribute("name");
                        String value = propElement.getAttribute("value");
                        tileProps.setProperty(name, value);
                    }
                }

                this.props.put(id, tileProps);
            }



        } else {
            throw new SlickException("TiledMap requires that the map be created with tilesets that use a single image.  Check the WiKi for more complete information.");
        }
    }

    public int getTileWidth() {
        return this.tileWidth;
    }

    public int getTileHeight() {
        return this.tileHeight;
    }

    public int getTileSpacing() {
        return this.tileSpacing;
    }

    public int getTileMargin() {
        return this.tileMargin;
    }

    public void setTileSetImage(Image image) {
        this.tiles = new SpriteSheet(image, this.tileWidth, this.tileHeight, this.tileSpacing, this.tileMargin);
        this.tilesAcross = this.tiles.getHorizontalCount();
        this.tilesDown = this.tiles.getVerticalCount();
        if (this.tilesAcross <= 0) {
            this.tilesAcross = 1;
        }

        if (this.tilesDown <= 0) {
            this.tilesDown = 1;
        }

        this.lastGID = this.tilesAcross * this.tilesDown + this.firstGID - 1;
    }

    public Properties getProperties(int globalID) {
        return (Properties)this.props.get(new Integer(globalID));
    }

    public int getTileX(int id) {
        return id % this.tilesAcross;
    }

    public int getTileY(int id) {
        return id / this.tilesAcross;
    }

    public void setLimit(int limit) {
        this.lastGID = limit;
    }

    public boolean contains(int gid) {
        return gid >= this.firstGID && gid <= this.lastGID;
    }

    public Animation getAnimation(int tileId) {
        return animatedTiles.get(tileId);
    }

    public boolean isAnimated(int tileId) {
        return animatedTiles.containsKey(tileId);
    }

}