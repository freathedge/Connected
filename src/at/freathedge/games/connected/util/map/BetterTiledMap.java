//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by FernFlower decompiler)
//

package at.freathedge.games.connected.util.map;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Properties;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import org.newdawn.slick.Image;
import org.newdawn.slick.SlickException;
import org.newdawn.slick.tiled.Layer;
import org.newdawn.slick.tiled.TileSet;
import org.newdawn.slick.util.Log;
import org.newdawn.slick.util.ResourceLoader;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.EntityResolver;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

public class BetterTiledMap {
    private static boolean headless;
    protected int width;
    protected int height;
    protected int tileWidth;
    protected int tileHeight;
    protected String tilesLocation;
    protected Properties props;
    protected ArrayList tileSets;
    protected ArrayList layers;
    protected ArrayList objectGroups;
    protected static final int ORTHOGONAL = 1;
    protected static final int ISOMETRIC = 2;
    protected int orientation;
    private boolean loadTileSets;

    private static void setHeadless(boolean h) {
        headless = h;
    }

    public BetterTiledMap(String ref) throws SlickException {
        this(ref, true);
    }

    public BetterTiledMap(String ref, boolean loadTileSets) throws SlickException {
        this.tileSets = new ArrayList();
        this.layers = new ArrayList();
        this.objectGroups = new ArrayList();
        this.loadTileSets = true;
        this.loadTileSets = loadTileSets;
        ref = ref.replace('\\', '/');
        this.load(ResourceLoader.getResourceAsStream(ref), ref.substring(0, ref.lastIndexOf("/")));
    }

    public BetterTiledMap(String ref, String tileSetsLocation) throws SlickException {
        this.tileSets = new ArrayList();
        this.layers = new ArrayList();
        this.objectGroups = new ArrayList();
        this.loadTileSets = true;
        this.load(ResourceLoader.getResourceAsStream(ref), tileSetsLocation);
    }

    public BetterTiledMap(InputStream in) throws SlickException {
        this.tileSets = new ArrayList();
        this.layers = new ArrayList();
        this.objectGroups = new ArrayList();
        this.loadTileSets = true;
        this.load(in, "");
    }

    public BetterTiledMap(InputStream in, String tileSetsLocation) throws SlickException {
        this.tileSets = new ArrayList();
        this.layers = new ArrayList();
        this.objectGroups = new ArrayList();
        this.loadTileSets = true;
        this.load(in, tileSetsLocation);
    }

    public String getTilesLocation() {
        return this.tilesLocation;
    }

    public int getLayerIndex(String name) {
        int idx = 0;

        for(int i = 0; i < this.layers.size(); ++i) {
            BetterLayer layer = (BetterLayer) this.layers.get(i);
            if (layer.name.equals(name)) {
                return i;
            }
        }

        return -1;
    }

    public Image getTileImage(int x, int y, int layerIndex) {
        BetterLayer layer = (BetterLayer) this.layers.get(layerIndex);
        int tileSetIndex = layer.data[x][y][0];
        if (tileSetIndex >= 0 && tileSetIndex < this.tileSets.size()) {
            BetterTileSet tileSet = (BetterTileSet) this.tileSets.get(tileSetIndex);
            int sheetX = tileSet.getTileX(layer.data[x][y][1]);
            int sheetY = tileSet.getTileY(layer.data[x][y][1]);
            return tileSet.tiles.getSprite(sheetX, sheetY);
        } else {
            return null;
        }
    }

    public int getWidth() {
        return this.width;
    }

    public int getHeight() {
        return this.height;
    }

    public int getTileHeight() {
        return this.tileHeight;
    }

    public int getTileWidth() {
        return this.tileWidth;
    }

    public int getTileId(int x, int y, int layerIndex) {
        BetterLayer layer = (BetterLayer) this.layers.get(layerIndex);
        return layer.getTileID(x, y);
    }

    public void setTileId(int x, int y, int layerIndex, int tileid) {
        Layer layer = (Layer)this.layers.get(layerIndex);
        layer.setTileID(x, y, tileid);
    }

    public String getMapProperty(String propertyName, String def) {
        return this.props == null ? def : this.props.getProperty(propertyName, def);
    }

    public String getLayerProperty(int layerIndex, String propertyName, String def) {
        Layer layer = (Layer)this.layers.get(layerIndex);
        return layer != null && layer.props != null ? layer.props.getProperty(propertyName, def) : def;
    }

    public String getTileProperty(int tileID, String propertyName, String def) {
        if (tileID == 0) {
            return def;
        } else {
            BetterTileSet set = this.findTileSet(tileID);
            Properties props = set.getProperties(tileID);
            return props == null ? def : props.getProperty(propertyName, def);
        }
    }

    public void render(int x, int y) {
        this.render(x, y, 0, 0, this.width, this.height, false);
    }

    public void render(int x, int y, int layer) {
        this.render(x, y, 0, 0, this.getWidth(), this.getHeight(), layer, false);
    }

    public void render(int x, int y, int sx, int sy, int width, int height) {
        this.render(x, y, sx, sy, width, height, false);
    }

    public void render(int x, int y, int sx, int sy, int width, int height, int l, boolean lineByLine) {
        Layer layer = (Layer)this.layers.get(l);
        switch (this.orientation) {
            case 1:
                for(int ty = 0; ty < height; ++ty) {
                    layer.render(x, y, sx, sy, width, ty, lineByLine, this.tileWidth, this.tileHeight);
                }
                break;
            case 2:
                this.renderIsometricMap(x, y, sx, sy, width, height, layer, lineByLine);
        }

    }

    public void render(int x, int y, int sx, int sy, int width, int height, boolean lineByLine) {
        switch (this.orientation) {
            case 1:
                for(int ty = 0; ty < height; ++ty) {
                    for(int i = 0; i < this.layers.size(); ++i) {
                        Layer layer = (Layer)this.layers.get(i);
                        layer.render(x, y, sx, sy, width, ty, lineByLine, this.tileWidth, this.tileHeight);
                    }
                }
                break;
            case 2:
                this.renderIsometricMap(x, y, sx, sy, width, height, (Layer)null, lineByLine);
        }

    }

    protected void renderIsometricMap(int x, int y, int sx, int sy, int width, int height, Layer layer, boolean lineByLine) {
        ArrayList drawLayers = this.layers;
        if (layer != null) {
            drawLayers = new ArrayList();
            drawLayers.add(layer);
        }

        int maxCount = width * height;
        int allCount = 0;
        boolean allProcessed = false;
        int initialLineX = x;
        int initialLineY = y;
        int startLineTileX = 0;
        int startLineTileY = 0;

        while(!allProcessed) {
            int currentTileX = startLineTileX;
            int currentTileY = startLineTileY;
            int currentLineX = initialLineX;
            int min = 0;
            if (height > width) {
                min = startLineTileY < width - 1 ? startLineTileY : (width - startLineTileX < height ? width - startLineTileX - 1 : width - 1);
            } else {
                min = startLineTileY < height - 1 ? startLineTileY : (width - startLineTileX < height ? width - startLineTileX - 1 : height - 1);
            }

            for(int burner = 0; burner <= min; ++burner) {
                for(int layerIdx = 0; layerIdx < drawLayers.size(); ++layerIdx) {
                    Layer currentLayer = (Layer)drawLayers.get(layerIdx);
                    currentLayer.render(currentLineX, initialLineY, currentTileX, currentTileY, 1, 0, lineByLine, this.tileWidth, this.tileHeight);
                }

                currentLineX += this.tileWidth;
                ++allCount;
                ++currentTileX;
                --currentTileY;
            }

            if (startLineTileY < height - 1) {
                ++startLineTileY;
                initialLineX -= this.tileWidth / 2;
                initialLineY += this.tileHeight / 2;
            } else {
                ++startLineTileX;
                initialLineX += this.tileWidth / 2;
                initialLineY += this.tileHeight / 2;
            }

            if (allCount >= maxCount) {
                allProcessed = true;
            }
        }

    }

    public int getLayerCount() {
        return this.layers.size();
    }

    private int parseInt(String value) {
        try {
            return Integer.parseInt(value);
        } catch (NumberFormatException var3) {
            return 0;
        }
    }

    private void load(InputStream in, String tileSetsLocation) throws SlickException {
        this.tilesLocation = tileSetsLocation;
        try {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            factory.setValidating(false);
            DocumentBuilder builder = factory.newDocumentBuilder();
            builder.setEntityResolver((publicId, systemId) -> new InputSource(new ByteArrayInputStream(new byte[0])));
            Document doc = builder.parse(in);

            Element mapElement = doc.getDocumentElement();
            this.orientation = mapElement.getAttribute("orientation").equals("orthogonal") ? ORTHOGONAL : ISOMETRIC;
            this.width = parseInt(mapElement.getAttribute("width"));
            this.height = parseInt(mapElement.getAttribute("height"));
            this.tileWidth = parseInt(mapElement.getAttribute("tilewidth"));
            this.tileHeight = parseInt(mapElement.getAttribute("tileheight"));

            Element propsElement = (Element) mapElement.getElementsByTagName("properties").item(0);
            if (propsElement != null) {
                NodeList propsList = propsElement.getElementsByTagName("property");
                props = new Properties();
                for (int i = 0; i < propsList.getLength(); i++) {
                    Element prop = (Element) propsList.item(i);
                    props.setProperty(prop.getAttribute("name"), prop.getAttribute("value"));
                }
            }

            if (loadTileSets) {
                NodeList setNodes = mapElement.getElementsByTagName("tileset");
                BetterTileSet lastSet = null;

                for (int i = 0; i < setNodes.getLength(); i++) {
                    Element current = (Element) setNodes.item(i);
                    BetterTileSet tileSet = new BetterTileSet(this, current, !headless);
                    tileSet.index = i;
                    if (lastSet != null) {
                        lastSet.setLimit(tileSet.firstGID - 1);
                    }
                    lastSet = tileSet;
                    tileSets.add(tileSet);
                }
            }

            NodeList layerNodes = mapElement.getElementsByTagName("layer");
            for (int i = 0; i < layerNodes.getLength(); i++) {
                Element current = (Element) layerNodes.item(i);
                BetterLayer layer = new BetterLayer(this, current);
                layer.index = i;
                layers.add(layer);
            }

            NodeList objectGroupNodes = mapElement.getElementsByTagName("objectgroup");
            for (int i = 0; i < objectGroupNodes.getLength(); i++) {
                Element current = (Element) objectGroupNodes.item(i);
                ObjectGroup objectGroup = new ObjectGroup(current);
                objectGroup.index = i;
                objectGroups.add(objectGroup);
            }

        } catch (Exception e) {
            Log.error(e);
            throw new SlickException("Failed to parse BetterTileMap", e);
        }
    }

    public int getTileSetCount() {
        return this.tileSets.size();
    }

    public TileSet getTileSet(int index) {
        return (TileSet)this.tileSets.get(index);
    }

    public TileSet getTileSetByGID(int gid) {
        for(int i = 0; i < this.tileSets.size(); ++i) {
            TileSet set = (TileSet)this.tileSets.get(i);
            if (set.contains(gid)) {
                return set;
            }
        }

        return null;
    }

    public BetterTileSet findTileSet(int gid) {
        for(int i = 0; i < this.tileSets.size(); ++i) {
            BetterTileSet set = (BetterTileSet) this.tileSets.get(i);
            if (set.contains(gid)) {
                return set;
            }
        }

        return null;
    }

    protected void renderedLine(int visualY, int mapY, int layer) {
    }

    public int getObjectGroupCount() {
        return this.objectGroups.size();
    }

    public int getObjectCount(int groupID) {
        if (groupID >= 0 && groupID < this.objectGroups.size()) {
            ObjectGroup grp = (ObjectGroup)this.objectGroups.get(groupID);
            return grp.objects.size();
        } else {
            return -1;
        }
    }

    public String getObjectName(int groupID, int objectID) {
        if (groupID >= 0 && groupID < this.objectGroups.size()) {
            ObjectGroup grp = (ObjectGroup)this.objectGroups.get(groupID);
            if (objectID >= 0 && objectID < grp.objects.size()) {
                GroupObject object = (GroupObject)grp.objects.get(objectID);
                return object.name;
            }
        }

        return null;
    }

    public String getObjectType(int groupID, int objectID) {
        if (groupID >= 0 && groupID < this.objectGroups.size()) {
            ObjectGroup grp = (ObjectGroup)this.objectGroups.get(groupID);
            if (objectID >= 0 && objectID < grp.objects.size()) {
                GroupObject object = (GroupObject)grp.objects.get(objectID);
                return object.type;
            }
        }

        return null;
    }

    public int getObjectX(int groupID, int objectID) {
        if (groupID >= 0 && groupID < this.objectGroups.size()) {
            ObjectGroup grp = (ObjectGroup)this.objectGroups.get(groupID);
            if (objectID >= 0 && objectID < grp.objects.size()) {
                GroupObject object = (GroupObject)grp.objects.get(objectID);
                return object.x;
            }
        }

        return -1;
    }

    public int getObjectY(int groupID, int objectID) {
        if (groupID >= 0 && groupID < this.objectGroups.size()) {
            ObjectGroup grp = (ObjectGroup)this.objectGroups.get(groupID);
            if (objectID >= 0 && objectID < grp.objects.size()) {
                GroupObject object = (GroupObject)grp.objects.get(objectID);
                return object.y;
            }
        }

        return -1;
    }

    public int getObjectWidth(int groupID, int objectID) {
        if (groupID >= 0 && groupID < this.objectGroups.size()) {
            ObjectGroup grp = (ObjectGroup)this.objectGroups.get(groupID);
            if (objectID >= 0 && objectID < grp.objects.size()) {
                GroupObject object = (GroupObject)grp.objects.get(objectID);
                return object.width;
            }
        }

        return -1;
    }

    public int getObjectHeight(int groupID, int objectID) {
        if (groupID >= 0 && groupID < this.objectGroups.size()) {
            ObjectGroup grp = (ObjectGroup)this.objectGroups.get(groupID);
            if (objectID >= 0 && objectID < grp.objects.size()) {
                GroupObject object = (GroupObject)grp.objects.get(objectID);
                return object.height;
            }
        }

        return -1;
    }

    public String getObjectImage(int groupID, int objectID) {
        if (groupID >= 0 && groupID < this.objectGroups.size()) {
            ObjectGroup grp = (ObjectGroup)this.objectGroups.get(groupID);
            if (objectID >= 0 && objectID < grp.objects.size()) {
                GroupObject object = (GroupObject)grp.objects.get(objectID);
                if (object == null) {
                    return null;
                }

                return object.image;
            }
        }

        return null;
    }

    public String getObjectProperty(int groupID, int objectID, String propertyName, String def) {
        if (groupID >= 0 && groupID < this.objectGroups.size()) {
            ObjectGroup grp = (ObjectGroup)this.objectGroups.get(groupID);
            if (objectID >= 0 && objectID < grp.objects.size()) {
                GroupObject object = (GroupObject)grp.objects.get(objectID);
                if (object == null) {
                    return def;
                }

                if (object.props == null) {
                    return def;
                }

                return object.props.getProperty(propertyName, def);
            }
        }

        return def;
    }

    protected class ObjectGroup {
        public int index;
        public String name;
        public ArrayList objects;
        public int width;
        public int height;
        public Properties props;

        public ObjectGroup(Element element) throws SlickException {
            this.name = element.getAttribute("name");
            this.width = Integer.parseInt(element.getAttribute("width"));
            this.height = Integer.parseInt(element.getAttribute("height"));
            this.objects = new ArrayList();
            Element propsElement = (Element)element.getElementsByTagName("properties").item(0);
            if (propsElement != null) {
                NodeList properties = propsElement.getElementsByTagName("property");
                if (properties != null) {
                    this.props = new Properties();

                    for(int p = 0; p < properties.getLength(); ++p) {
                        Element propElement = (Element)properties.item(p);
                        String name = propElement.getAttribute("name");
                        String value = propElement.getAttribute("value");
                        this.props.setProperty(name, value);
                    }
                }
            }

            NodeList objectNodes = element.getElementsByTagName("object");

            for(int i = 0; i < objectNodes.getLength(); ++i) {
                Element objElement = (Element)objectNodes.item(i);
                GroupObject object = BetterTiledMap.this.new GroupObject(objElement);
                object.index = i;
                this.objects.add(object);
            }

        }
    }

    protected class GroupObject {
        public int index;
        public String name;
        public String type;
        public int x;
        public int y;
        public int width;
        public int height;
        private String image;
        public Properties props;

        public GroupObject(Element element) throws SlickException {
            this.name = element.getAttribute("name");
            this.type = element.getAttribute("type");
            this.x = Integer.parseInt(element.getAttribute("x"));
            this.y = Integer.parseInt(element.getAttribute("y"));
            this.width = Integer.parseInt(element.getAttribute("width"));
            this.height = Integer.parseInt(element.getAttribute("height"));
            Element imageElement = (Element)element.getElementsByTagName("image").item(0);
            if (imageElement != null) {
                this.image = imageElement.getAttribute("source");
            }

            Element propsElement = (Element)element.getElementsByTagName("properties").item(0);
            if (propsElement != null) {
                NodeList properties = propsElement.getElementsByTagName("property");
                if (properties != null) {
                    this.props = new Properties();

                    for(int p = 0; p < properties.getLength(); ++p) {
                        Element propElement = (Element)properties.item(p);
                        String name = propElement.getAttribute("name");
                        String value = propElement.getAttribute("value");
                        this.props.setProperty(name, value);
                    }
                }
            }

        }
    }
}
