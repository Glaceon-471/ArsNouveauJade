package ArsNouveauJade;

import com.hollingsworth.arsnouveau.common.block.*;
import net.neoforged.fml.common.Mod;
import snownee.jade.api.IWailaClientRegistration;
import snownee.jade.api.IWailaCommonRegistration;
import snownee.jade.api.IWailaPlugin;
import snownee.jade.api.WailaPlugin;

@Mod(ArsNouveauJade.ModId)
@WailaPlugin
public class ArsNouveauJade implements IWailaPlugin {
    public static final String ModId = "ars_nouveau_jade";

    @Override
    public void register(IWailaCommonRegistration registration) {
        registration.registerBlockDataProvider(JadeComponentProvider.AgronomicSourcelink, AgronomicSourcelinkBlock.class);
        registration.registerBlockDataProvider(JadeComponentProvider.AlchemicalSourcelink, AlchemicalSourcelinkBlock.class);
        registration.registerBlockDataProvider(JadeComponentProvider.MycelialSourcelink, MycelialSourcelinkBlock.class);
        registration.registerBlockDataProvider(JadeComponentProvider.VitalicSourcelink, VitalicSourcelinkBlock.class);
        registration.registerBlockDataProvider(JadeComponentProvider.VolcanicSourcelink, VolcanicSourcelinkBlock.class);
        registration.registerBlockDataProvider(JadeComponentProvider.SourceJar, SourceJar.class);
        registration.registerBlockDataProvider(JadeComponentProvider.Imbuement, ImbuementBlock.class);
    }

    @Override
    public void registerClient(IWailaClientRegistration registration) {
        registration.registerBlockComponent(JadeComponentProvider.AgronomicSourcelink, AgronomicSourcelinkBlock.class);
        registration.registerBlockComponent(JadeComponentProvider.AlchemicalSourcelink, AlchemicalSourcelinkBlock.class);
        registration.registerBlockComponent(JadeComponentProvider.MycelialSourcelink, MycelialSourcelinkBlock.class);
        registration.registerBlockComponent(JadeComponentProvider.VitalicSourcelink, VitalicSourcelinkBlock.class);
        registration.registerBlockComponent(JadeComponentProvider.VolcanicSourcelink, VolcanicSourcelinkBlock.class);
        registration.registerBlockComponent(JadeComponentProvider.SourceJar, SourceJar.class);
        registration.registerBlockComponent(JadeComponentProvider.EnchantingApparatus, EnchantingApparatusBlock.class);
        registration.registerBlockComponent(JadeComponentProvider.Imbuement, ImbuementBlock.class);
    }
}
