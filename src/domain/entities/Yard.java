package domain.entities;

import domain.dtos.BundleDto;
import helpers.CenteredRectangle;
import helpers.Converter;
import helpers.GeomHelper;

import helpers.Point2D;

import java.io.Serializable;
import java.util.*;

public class Yard implements Serializable {
    private static final long serialVersionUID = 15641321L;
    private Map<String, Bundle> bundles;
    public Bundle lastBundleCreated;
    private Lift lift;

    public Yard() {
        setBundles(new HashMap<>());
        //setLift(new Lift());
    }

    public Yard(Map<String, Bundle> bundles) {
        setBundles(bundles);
        //setLift(lift);
    }

    public List<Bundle> getBundles() {
        return new ArrayList<>(this.bundles.values());
    }

    public void setBundles(Map<String, Bundle> bundles) { this.bundles = bundles; }

    //public Lift getLift() { return lift; }

   // public void setLift(Lift lift) { this.lift = lift; }

    public Bundle createBundle(Point2D position) {
        Bundle bundle = new Bundle(position);
        adjustBundleHeight(bundle);
        bundles.put(bundle.getId(), bundle);
        lastBundleCreated = bundle;
        return bundle;
    }

    private void adjustBundleHeight(Bundle bundle) {
        double maxZ = 0;
        for (Bundle collidingBundle : getCollidingBundles(bundle)) {
            double bundleTopZ = collidingBundle.z + collidingBundle.getHeight();
            if (bundleTopZ > maxZ) maxZ = bundleTopZ;
        }

        bundle.setZ(maxZ);
    }

    public Bundle getBundle(String id) {
        return bundles.get(id);
    }

    public List<Bundle> getBundlesAtPosition(Point2D position) {
        List<Bundle> selectedBundles = new ArrayList<>();
        for (Bundle bundle : new ArrayList<>(bundles.values())) {
            CenteredRectangle rectangle = Converter.fromBundleToCenteredRectangle(bundle);
            if (GeomHelper.pointIsInsideRectangle(position, rectangle)) {
                selectedBundles.add(bundle);
            }
        }

        return selectedBundles;
    }

    public void deleteBundle(String id) {
        bundles.remove(id);
    }


    public void modifyBundleProperties(BundleDto bundleDto)
    {
        Bundle bundle = getBundle(bundleDto.id);
        bundle.setBarcode(bundleDto.barcode);
        bundle.setHeight(bundleDto.height);
        bundle.setWidth(bundleDto.width);
        bundle.setLength(bundleDto.length);
        bundle.setTime(bundleDto.time);
        bundle.setDate(bundleDto.date);
        bundle.setEssence(bundleDto.essence);
        bundle.setPlanckSize(bundleDto.plankSize);
        bundle.setAngle(bundleDto.angle);
    }

    public void modifyBundlePosition(String id, Point2D position)
    {
        Bundle bundle = getBundle(id);
        bundle.setPosition(position);
        adjustBundleHeight(bundle);
    }

    public List<Bundle> getCollidingBundles(Bundle bundleToCheck) {
        List<Bundle> collidingBundles = new ArrayList<>();
        for (Bundle bundle : getBundles()) {
            if (bundle != bundleToCheck) {
                boolean bundleCollides = GeomHelper.rectangleCollidesRectangle(
                        Converter.fromBundleToCenteredRectangle(bundle),
                        Converter.fromBundleToCenteredRectangle(bundleToCheck)
                );
                if (bundleCollides) {
                    collidingBundles.add(bundle);
                }
            }
        }
        return collidingBundles;
    }
}
