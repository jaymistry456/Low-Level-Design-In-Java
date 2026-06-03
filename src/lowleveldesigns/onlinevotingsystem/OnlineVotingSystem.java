package lowleveldesigns.onlinevotingsystem;

import java.time.LocalDate;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

/*
enums
* */
enum ElectionState {
    UPCOMING,
    IN_PROGRESS,
    COMPLETED,
    CANCELLED;
}

enum VoterState {
    UNVERIFIED,
    VERIFIED;
}

/*
classes
* */
/*
Voter
Candidate
Admin
Election
OnlineVotingSystem
* */

/*
Voter
    knows:
        voterId
        voterName
        voterPhoneNo
        VoterState
    does:
        verify()
* */
class Voter {
    private String voterId;
    private String voterName;
    private String voterPhoneNo;
    private VoterState voterState;

    public Voter(String voterId, String voterName, String voterPhoneNo) {
        this.voterId = voterId;
        this.voterName = voterName;
        this.voterPhoneNo = voterPhoneNo;
        this.voterState = VoterState.UNVERIFIED;
    }

    public String getVoterId() {
        return voterId;
    }

    public String getVoterName() {
        return voterName;
    }

    public String getVoterPhoneNo() {
        return voterPhoneNo;
    }

    public VoterState getVoterState() {
        return voterState;
    }

    public void verify() {
        this.voterState = VoterState.VERIFIED;
    }
}

/*
Candidate
    knows:
        candidateId
        candidateName
        candidatePhoneNo
    does:
        nothing (data carrier)
* */
class Candidate {
    private String candidateId;
    private String candidateName;
    private String candidatePhoneNo;

    public Candidate(String candidateId, String candidateName, String candidatePhoneNo) {
        this.candidateId = candidateId;
        this.candidateName = candidateName;
        this.candidatePhoneNo = candidatePhoneNo;
    }

    public String getCandidateId() {
        return candidateId;
    }

    public String getCandidateName() {
        return candidateName;
    }

    public String getCandidatePhoneNo() {
        return candidatePhoneNo;
    }
}

/*
Election
    knows:
        electionId
        electionName
        Map<Candidate, AtomicInteger> voteCounts
        Set<String> votedVoterIds
        ElectionState
        electionDate
    does:
        vote(Voter, Candidate)
        getCount(Candidate) -> int
        getWinner() -> Candidate
* */
class Election {
    private String electionId;
    private String electionName;
    private Map<Candidate, AtomicInteger> voteCounts;
    private Set<String> votedVoterIds;
    private ElectionState electionState;
    private LocalDate electionDate;
    private final Object voteLock = new Object();

    public Election(String electionId, String electionName, LocalDate electionDate) {
        this.electionId = electionId;
        this.electionName = electionName;
        this.voteCounts = new HashMap<>();
        this.votedVoterIds = new HashSet<>();
        this.electionState = ElectionState.UPCOMING;
        this.electionDate = electionDate;
    }

    public String getElectionId() {
        return electionId;
    }

    public String getElectionName() {
        return electionName;
    }

    public Map<Candidate, AtomicInteger> getVoteCounts() {
        return voteCounts;
    }

    public Set<String> getVotedVoterIds() {
        return votedVoterIds;
    }

    public ElectionState getElectionState() {
        return electionState;
    }

    public LocalDate getElectionDate() {
        return electionDate;
    }

    public void setElectionState(ElectionState electionState) {
        this.electionState = electionState;
    }

    public void setElectionDate(LocalDate electionDate) {
        this.electionDate = electionDate;
    }

    public void vote(Voter voter, Candidate candidate) {
        synchronized (voteLock) {
            if(electionState != ElectionState.IN_PROGRESS ||
                    votedVoterIds.contains(voter.getVoterId()) ||
                    voter.getVoterState() != VoterState.VERIFIED ||
                    !voteCounts.containsKey(candidate)
            ) {
                return;
            }

            votedVoterIds.add(voter.getVoterId());
            voteCounts.get(candidate).incrementAndGet();
        }
    }

    public int getCount(Candidate candidate) {
        return voteCounts.get(candidate).get();
    }

    public Candidate getWinner() {
        if(electionState != ElectionState.COMPLETED) return null;

        Candidate result = null;
        int maxVotes = 0;
        for(Map.Entry<Candidate, AtomicInteger> entry: voteCounts.entrySet()) {
            if(entry.getValue().get() > maxVotes) {
                result = entry.getKey();
                maxVotes = entry.getValue().get();
            }
        }

        return result;
    }
}

/*
OnlineVotingSystem
    knows:
        List<Voter>
        List<Candidate>
        List<Election>
    does:
        addVoter(Voter)
        removeVoter(Voter)
        verifyVoter(Voter)
        addCandidate(Candidate)
        removeCandidate(Candidate)
        addElection(Election)
        removeElection(Election)
        startElection(Election)
        endElection(Election)
        cancelElection(Election)
        vote(Election, Voter, Candidate)
        getVoteCount(Election, Candidate)
* */
public class OnlineVotingSystem {
    private List<Voter> voters;
    private List<Candidate> candidates;
    private List<Election> elections;

    public OnlineVotingSystem() {
        this.voters = new ArrayList<>();
        this.candidates = new ArrayList<>();
        this.elections = new ArrayList<>();
    }

    public void addVoter(Voter voter) {
        voters.add(voter);
    }

    public void removeVoter(Voter voter) {
        voters.remove(voter);
    }

    public void verifyVoter(Voter voter) {
        voter.verify();
    }

    public void addCandidate(Candidate candidate) {
        candidates.add(candidate);
    }

    public void removeCandidate(Candidate candidate) {
        candidates.remove(candidate);
    }

    public void addElection(Election election) {
        elections.add(election);
    }

    public void removeElection(Election election) {
        elections.remove(election);
    }

    public void startElection(Election election) {
        election.setElectionState(ElectionState.IN_PROGRESS);
    }

    public void endElection(Election election) {
        election.setElectionState(ElectionState.COMPLETED);
    }

    public void cancelElection(Election election) {
        election.setElectionState(ElectionState.CANCELLED);
    }

    public void vote(Election election, Voter voter, Candidate candidate) {
        election.vote(voter, candidate);
    }

    public int getCandidateVoteCount(Election election, Candidate candidate) {
        return election.getCount(candidate);
    }

    public Candidate getWinner(Election election) {
        return election.getWinner();
    }
}

/*

Election has-a Map of Candidate -> voteCount
Election has-a ElectionState

OnlineVotingSystem has-a List of Voter
OnlineVotingSystem has-a List of Candidate
OnlineVotingSystem has-a List of Election

* */