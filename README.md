# Simple Opaque WDM Simulator with Anycast Service Requests written in Java

### This simulator is under development and will be updated soon with more resources and documentation

Simple simulator implemented in Java (version >=8) for simulating opaque WDM networks, e.g, optical networks containing wavelength conversion at each node.
This means that the lightpaths do not need to enforce the wavelength continuity constraint.
In simpler terms, the infrastructure simulated in this project can be understood as a circuit-switched network with anycast traffic, i.e., the destination can be selected by the routing algorithm given some criteria.
For more info, see [this paper](https://ieeexplore.ieee.org/abstract/document/767791).
The service requests are modeled as anycast, i.e., the service destination can be selected among the data centers available in the network.
The simulator implements the basic funcionalities, and allows you to add more complex features for your particular purposes.
The simulator offers multi-threading for running multiple scenarios using a pool of threads, taking advantage of high-performance computing environments.

### Dependencies:

This code was validated using Java 11. The software has the following dependencies:

- Gradle (for dependency and build management)
- Log4J (for advanced logging features)
- jGraphT (for graph manipulation and path computation)
- Typesafe Config (for configuration file reading)

### Code organization and features:

The following algorithms are implemented:
- **Closest available data center (CADC)**: selects the the data center with enough capacity that has the shortest available path.
- **Full load balancing (FLB)**: selects the path and data center with the lowest combined load, i.e., the combined load is computed as the multiplication of the path and data center current usage.

An analysis of the routing/placement algorithms can be found [here](https://ieeexplore.ieee.org/abstract/document/6294216).

### Citing this software

This software was developed as part of the following research paper:

C. N. da Silva, L. Wosinska, S. Spadaro, J. C. W. A. Costa, C. R. L. Frances and P. Monti, "Restoration in optical cloud networks with relocation and services differentiation," in IEEE/OSA Journal of Optical Communications and Networking, vol. 8, no. 2, pp. 100-111, Feb. 2016. DOI: 10.1364/JOCN.8.000100. [Open access](http://www.diva-portal.org/smash/record.jsf?pid=diva2%3A925332&dswid=-6552)

BibTeX entry:

~~~~
@ARTICLE{Natalino:2016:jocn,
    author={C. N. {da Silva} and L. {Wosinska} and S. {Spadaro} and J. C. W. A. {Costa} and C. R. L. {Frances} and P. {Monti}},
    journal={IEEE/OSA Journal of Optical Communications and Networking},
    title={Restoration in optical cloud networks with relocation and services differentiation},
    year={2016},
    volume={8},
    number={2},
    pages={100-111},
    doi={10.1364/JOCN.8.000100},
    ISSN={1943-0620},
    month={Feb},
    }
~~~~
