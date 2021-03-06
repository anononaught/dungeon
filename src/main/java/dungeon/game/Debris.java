package dungeon.game;

import dungeon.Main;
import org.jbox2d.common.Vec2;
import org.jbox2d.dynamics.Body;

import java.util.Collection;

interface Debris {

    Body getBody();
    float getMass();

    void updatePosition(Collection<Debris> debris);

    default void updateSpaceWrap() {
        if (!getBody().isAwake()) {
            return;
        }

        Vec2 this_position = this.getBody().getWorldCenter();
        Vec2 that_position = this_position.clone();

        if (this_position.x > Main.WIDTH) {
            that_position.x = 0;
        } else if (this_position.x < 0) {
            that_position.x = Main.WIDTH;
        }

        if (this_position.y > Main.HEIGHT) {
            that_position.y = 0;
        } else if (this_position.y < 0) {
            that_position.y = Main.HEIGHT;
        }

        if (!this_position.equals(that_position)) {
            this.getBody().setTransform(that_position, this.getBody().getAngle());
        }
    }

    default void updateForce(Collection<Debris> debris) {
        if (!getBody().isAwake()) {
            return;
        }

        debris.forEach(that -> {

            Vec2 this_position = this.getBody().getWorldCenter();
            Vec2 that_position = that.getBody().getWorldCenter();

            if (this_position == that_position) {
                return;
            }

            // Vector that is used to calculate the distance of the debris to the planet and what force to apply to the debris.
            Vec2 distance = new Vec2(0, 0);

            // Add the distance to the debris
            distance = distance.add(this_position);

            // Subtract the distance to the planet's position to get the vector between the debris and the planet.
            distance = distance.sub(that_position);

            // Check if the distance between debris is within the reach of the gravitational pull.
            if (distance.length() < that.getMass() * 4) {

                // Calculate the magnitude of the force to apply to the debris.
                // This is proportional to the distance between the planet and the debris. The force is weaker the further away the debris.
                float force = (float)((2f * that.getMass()) / Math.pow(distance.length(), 2));

                // Change the direction of the vector so that the force will be towards the planet.
                distance = distance.negate();

                // Multiply the magnitude of the force to the directional vector.
                distance = distance.mul(force);
                this.getBody().applyForceToCenter(distance);
            }
        });
    }
}
