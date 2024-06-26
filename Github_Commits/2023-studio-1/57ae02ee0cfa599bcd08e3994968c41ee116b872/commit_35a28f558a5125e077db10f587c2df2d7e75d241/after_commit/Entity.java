package com.csse3200.game.entities;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.IntMap;
import com.badlogic.gdx.utils.Json;
import com.badlogic.gdx.utils.JsonValue;
import com.badlogic.gdx.utils.Null;
import com.csse3200.game.areas.terrain.CropTileComponent;
import com.csse3200.game.components.Component;
import com.csse3200.game.components.ComponentType;
import com.csse3200.game.components.items.ItemComponent;
import com.csse3200.game.components.items.ItemType;
import com.csse3200.game.components.items.WateringCanLevelComponent;
import com.csse3200.game.components.npc.TamableComponent;
import com.csse3200.game.components.plants.PlantComponent;
import com.csse3200.game.components.player.InventoryComponent;
import com.csse3200.game.components.player.KeyboardPlayerInputComponent;
import com.csse3200.game.components.player.PlayerActions;
import com.csse3200.game.components.tractor.TractorActions;
import com.csse3200.game.events.EventHandler;
import com.csse3200.game.services.FactoryService;
import com.csse3200.game.services.ResourceService;
import com.csse3200.game.services.ServiceLocator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.awt.*;
import java.util.ArrayList;
import java.util.HashMap;

/**
 * Core entity class. Entities exist in the game and are updated each frame. All
 * entities have a
 * position and scale, but have no default behaviour. Components should be added
 * to an entity to
 * give it specific behaviour. This class should not be inherited or modified
 * directly.
 *
 * <p>
 * Example use:
 *
 * <pre>
 * Entity player = new Entity()
 *     .addComponent(new RenderComponent())
 *     .addComponent(new PlayerControllerComponent());
 * ServiceLocator.getEntityService().register(player);
 * </pre>
 */
public class Entity implements Json.Serializable {
  private static final Logger logger = LoggerFactory.getLogger(Entity.class);
  private static int nextId = 0;
  private static final String EVT_NAME_POS = "setPosition";

  private final int id;
  private EntityType type;
  private final IntMap<Component> components;
  private final EventHandler eventHandler;
  private boolean enabled = true;
  private boolean created = false;
  private Vector2 position = Vector2.Zero.cpy();
  private Vector2 scale = new Vector2(1, 1);
  private Array<Component> createdComponents;

  public Entity() {
    this.type = null;
    id = nextId;
    nextId++;

    components = new IntMap<>(4);
    eventHandler = new EventHandler();
  }

  public Entity(EntityType type) {
    this.type = type;
    id = nextId;
    nextId++;

    components = new IntMap<>(4);
    eventHandler = new EventHandler();
  }

  /**
   * Enable or disable an entity. Disabled entities do not run update() or
   * earlyUpdate() on their
   * components, but can still be disposed.
   *
   * @param enabled true for enable, false for disable.
   */
  public void setEnabled(boolean enabled) {
    logger.debug("Setting enabled={} on entity {}", enabled, this);
    this.enabled = enabled;
  }

  /**
   * Get the entity's game position.
   *
   * @return position
   */
  public Vector2 getPosition() {
    return position.cpy(); // Cpy gives us pass-by-value to prevent bugs
  }

  /**
   * Set the entity's game position.
   *
   * @param position new position.
   */
  public void setPosition(Vector2 position) {
    this.position = position.cpy();
    getEvents().trigger(EVT_NAME_POS, position.cpy());
  }

  /**
   * Set the entity's game position.
   *
   * @param x new x position
   * @param y new y position
   */
  public void setPosition(float x, float y) {
    this.position.x = x;
    this.position.y = y;
    getEvents().trigger(EVT_NAME_POS, position.cpy());
  }

  /**
   * Set the entity's game position and optionally notifies listeners.
   *
   * @param position new position.
   * @param notify   true to notify (default), false otherwise
   */
  public void setPosition(Vector2 position, boolean notify) {
    this.position = position;
    if (notify) {
      getEvents().trigger(EVT_NAME_POS, position);
    }
  }

  /**
   * Get the entity's scale. Used for rendering and physics bounding box
   * calculations.
   *
   * @return Scale in x and y directions. 1 = 1 metre.
   */
  public Vector2 getScale() {
    return scale.cpy(); // Cpy gives us pass-by-value to prevent bugs
  }

  /**
   * Set the entity's scale.
   *
   * @param scale new scale in metres
   */
  public void setScale(Vector2 scale) {
    this.scale = scale.cpy();
  }

  /**
   * Set the entity's scale.
   *
   * @param x width in metres
   * @param y height in metres
   */
  public void setScale(float x, float y) {
    this.scale.x = x;
    this.scale.y = y;
  }

  /**
   * Set the entity's width and scale the height to maintain aspect ratio.
   *
   * @param x width in metres
   */
  public void scaleWidth(float x) {
    this.scale.y = this.scale.y / this.scale.x * x;
    this.scale.x = x;
  }

  /**
   * Set the entity's height and scale the width to maintain aspect ratio.
   *
   * @param y height in metres
   */
  public void scaleHeight(float y) {
    this.scale.x = this.scale.x / this.scale.y * y;
    this.scale.y = y;
  }

  /**
   * Get the entity's center position
   *
   * @return center position
   */
  public Vector2 getCenterPosition() {
    return getPosition().mulAdd(getScale(), 0.5f);
  }

  /**
   * Get a component of type T on the entity.
   *
   * @param type The component class, e.g. RenderComponent.class
   * @param <T>  The component type, e.g. RenderComponent
   * @return The entity component, or null if nonexistent.
   */
  @SuppressWarnings("unchecked")
  public <T extends Component> T getComponent(Class<T> type) {
    ComponentType componentType = ComponentType.getFrom(type);
    return (T) components.get(componentType.getId());
  }

  /**
   * Add a component to the entity. Can only be called before the entity is
   * registered in the world.
   *
   * @param component The component to add. Only one component of a type can be
   *                  added to an entity.
   * @return Itself
   */
  public Entity addComponent(Component component) {
    if (created) {
      logger.error(
          "Adding {} to {} after creation is not supported and will be ignored", component, this);
      return this;
    }
    ComponentType componentType = ComponentType.getFrom(component.getClass());
    if (components.containsKey(componentType.getId())) {
      logger.error(
          "Attempted to add multiple components of class {} to {}. Only one component of a class "
              + "can be added to an entity, this will be ignored.",
          component,
          this);
      return this;
    }
    components.put(componentType.getId(), component);
    component.setEntity(this);

    return this;
  }

  /**
   * Dispose of the entity. This will dispose of all components on this entity.
   */
  public void dispose() {
    for (Component component : createdComponents) {
      component.dispose();
    }
    ServiceLocator.getEntityService().unregister(this);
  }

  /**
   * Create the entity and start running. This is called when the entity is
   * registered in the world,
   * and should not be called manually.
   */
  public void create() {
    if (created) {
      logger.error(
          "{} was created twice. Entity should only be registered with the entity service once.",
          this);
      return;
    }
    createdComponents = components.values().toArray();
    for (Component component : createdComponents) {
      component.create();
    }
    created = true;
  }

  /**
   * Perform an early update on all components. This is called by the entity
   * service and should not
   * be called manually.
   */
  public void earlyUpdate() {
    if (!enabled) {
      return;
    }
    for (Component component : createdComponents) {
      component.triggerEarlyUpdate();
    }
  }

  /**
   * Perform an update on all components. This is called by the entity service and
   * should not be
   * called manually.
   */
  public void update() {
    if (!enabled) {
      return;
    }
    for (Component component : createdComponents) {
      component.triggerUpdate();
    }
  }

  /**
   * This entity's unique ID. Used for equality checks
   *
   * @return unique ID
   */
  public int getId() {
    return id;
  }

  /**
   * Get the event handler attached to this entity. Can be used to trigger events
   * from an attached
   * component, or listen to events from a component.
   *
   * @return entity's event handler
   */
  public EventHandler getEvents() {
    return eventHandler;
  }

  @Override
  public boolean equals(Object obj) {
    return (obj instanceof Entity && ((Entity) obj).getId() == this.getId());
  }

  @Override
  public int hashCode() {
    return super.hashCode();
  }

  @Override
  public String toString() {
    return String.format("Entity{id=%d}", id);
  }

  /**
   * Writes to the json info about entities.
   * Writes the entities x,y coordinates
   * ALso loops through the entities associated components and writes information
   * to the json about
   * the component.
   * note each component should have a override write function
   * 
   * @param json which is a valid Json that is written to
   */
  public void write(Json json) {
    // Should be gone but incase double check
    if (getType() == EntityType.Item || getType() == null) {
      return;
    }

    json.writeValue("Entity", getType());
    float posX = position.x;
    float posY = position.y;
    json.writeValue("x", posX);
    json.writeValue("y", posY);
    json.writeObjectStart("components");
    for (Component c : createdComponents) {
      c.write(json);
    }
    json.writeObjectEnd();
  }

  /**
   * Writes the item to the json file
   * @param json which is a valid Json that is written to
   */
  public void writeItem(Json json) {
    json.writeValue("name", this.getComponent(ItemComponent.class).getItemName());
    // update the tractor 'muted' variable based on the info in the json file on
    // ItemType or something?
    if (this.getComponent(WateringCanLevelComponent.class) != null) {
      this.getComponent(WateringCanLevelComponent.class).write(json);
    }
  }

  /**
   * Reads the json file and creates the entities based on the information in the
   * json file
   * 
   * @param json    which is a valid Json that is read from
   * @param jsonMap which is a valid JsonValue that is read from
   */
  public void read(Json json, JsonValue jsonMap) {
    // Saves the position
    position = new Vector2(jsonMap.getFloat("x"), jsonMap.getFloat("y"));
    // Gets the type of Entity
    String value = jsonMap.getString("Entity");
    try {
      type = EntityType.valueOf(value);
    } catch (IllegalArgumentException e) {
      type = null;
    }

    if (type != null) { // Line 388 may break the switch statement otherwise
      switch (type) {
        case Tractor:
          jsonMap = jsonMap.get("components").get("TractorActions");
          TractorActions tractorActions = new TractorActions();
          // Update the tractor 'muted' variable based on the info in the json file
          tractorActions.setMuted(jsonMap.getBoolean("isMuted"));
          this.addComponent(tractorActions);
          break;
        case Tile:
          jsonMap = jsonMap.get("components").get("CropTileComponent");
          CropTileComponent c = new CropTileComponent(jsonMap.getFloat("waterContent"), jsonMap.getFloat("soilQuality"));
          c.setFertilised(jsonMap.getBoolean("isFertilised"));
          JsonValue plantData = jsonMap.get("plant");
          if (plantData.get("Entity") != null) {
            // Has a plant
            plantData = plantData.get("components").get("PlantComponent");
            c.setPlant(
                new Entity().addComponent(new PlantComponent(plantData.getInt("health"), plantData.getString("name"),
                    "bleh", "bleh", 1f, 999, 999, this.getComponent(CropTileComponent.class))));
            c.getPlant().getComponent(PlantComponent.class).setCurrentAge(plantData.getFloat("age"));
            c.getPlant().getComponent(PlantComponent.class).setGrowthStage(plantData.getInt("growth"));
          }
          this.addComponent(c);
          break;
        case Cow:
        case Astrolotl:
        case Chicken:
          jsonMap = jsonMap.get("components").get("TamableComponent");
          // Does not need actual values here as it is just used to store the tamed value;
          Entity emptyPlayer = new Entity(); // empty player, as the real player is not needed for reading in the
                                             // component
          TamableComponent tamableComponent = new TamableComponent(emptyPlayer, 1, 1, null);
          tamableComponent.setTame(jsonMap.getBoolean("Tamed"));
          this.addComponent(tamableComponent);
          break;
        case Player:
          InventoryComponent inventoryComponent = new InventoryComponent(null);
          HashMap<Entity, Integer> items = new HashMap<>();
          HashMap<Entity, Point> itemPositions = new HashMap<>();
          ArrayList inventory = new ArrayList();
          JsonValue inv = jsonMap.get("components").get("InventoryComponent").get("inventory");
          inv.forEach(jsonValue -> {
            Entity item = FactoryService.getItemFactories().get(jsonValue.getString("name")).get();
            ItemType itemType = item.getComponent(ItemComponent.class).getItemType();
            switch (itemType) {
              case WATERING_CAN ->
                item.getComponent(WateringCanLevelComponent.class).setCurrentLevel(jsonValue.getFloat("level"));
            }
            items.put(item, jsonValue.getInt("count"));
            itemPositions.put(item, new Point(jsonValue.getInt("X"), jsonValue.getInt("Y")));
            inventory.add(item);
          });
          inventoryComponent.setInventory(items, itemPositions, inventory);
          this.addComponent(inventoryComponent);

        default:
          // Nothing
      }
    }
  }

  /**
   * Gets the type of entity
   * 
   * @return the type of entity from EntityType enum
   */
  public EntityType getType() {
    return type;
  }
}
